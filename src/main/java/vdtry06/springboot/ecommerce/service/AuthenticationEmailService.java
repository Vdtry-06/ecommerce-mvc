package vdtry06.springboot.ecommerce.service;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.constant.PredefinedRole;
import vdtry06.springboot.ecommerce.dto.request.RegisterUserRequest;
import vdtry06.springboot.ecommerce.dto.request.VerifyUserRequest;
import vdtry06.springboot.ecommerce.dto.response.RegisterUserResponse;
import vdtry06.springboot.ecommerce.entity.Role;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.mapper.UserMapper;
import vdtry06.springboot.ecommerce.repository.RoleRepository;
import vdtry06.springboot.ecommerce.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationEmailService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    UserMapper userMapper;
    KafkaProducerService kafkaProducerService;

    public RegisterUserResponse signup(RegisterUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) throw new AppException(ErrorCode.EMAIL_EXISTED);
        if (userRepository.existsByUsername(request.getUsername())) throw new AppException(ErrorCode.USERNAME_EXISTED);

        User user = userMapper.toUser(request);

        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEnabled(false);
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));

        Role role = roleRepository.findById(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        user.setRole(role);

        try {
            user = userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.USERNAME_EXISTED);
        }

        sendVerificationEmail(user);

        return userMapper.toRegisterUserResponse(user);
    }

    public void verifyUserSignup(VerifyUserRequest request) {
        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if(optionalUser.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        User user = optionalUser.get();
        if(user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.CODE_EXPIRED);
        }

        if(!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new AppException(ErrorCode.INVALID_CODE);
        }

        user.setEnabled(true);
        user.setVerificationCode(null);
        user.setVerificationExpiration(null);
        userRepository.save(user);
    }

    public RegisterUserResponse resendVerificationCode(String email) {
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalUser.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        User user = optionalUser.get();
        if(user.isEnabled()) {
            throw new AppException(ErrorCode.ACCOUNT_VERIFIED);
        }

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        sendVerificationEmail(user);
        userRepository.save(user);
        return userMapper.toRegisterUserResponse(user);
    }

    private void sendVerificationEmail(User user) {
        String topic = "verification-codes";
        String message = user.getEmail() + "," + user.getVerificationCode();
        kafkaProducerService.sendMessage(topic, message);
    }


    private String generateVerificationCode() {
        Random random = new Random();
        int code = random.nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
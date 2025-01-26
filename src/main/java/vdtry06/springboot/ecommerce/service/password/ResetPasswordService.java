package vdtry06.springboot.ecommerce.service.password;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.request.password.ResetPassword;
import vdtry06.springboot.ecommerce.dto.request.password.SendEmailRequest;
import vdtry06.springboot.ecommerce.dto.response.user.RegisterUserResponse;
import vdtry06.springboot.ecommerce.dto.response.user.UserResponse;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.mapper.UserMapper;
import vdtry06.springboot.ecommerce.repository.UserRepository;
import vdtry06.springboot.ecommerce.service.kafka.KafkaProducerService;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ResetPasswordService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    KafkaProducerService kafkaProducerService;
    ConcurrentHashMap<String, String> emailVerificationMap = new ConcurrentHashMap<>();


    public RegisterUserResponse sendEmail(SendEmailRequest request) {
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        user.setVerificationCode(generateVerificationCode());
        user.setVerificationExpiration(LocalDateTime.now().plusMinutes(15));
        emailVerificationMap.put(request.getEmail(), user.getVerificationCode());

        sendVerificationEmail(user);
        user = userRepository.save(user);

        return userMapper.toRegisterUserResponse(user);
    }


    public UserResponse verifyCodeAndResetPassword(ResetPassword request) {

        String sentVerificationCode = emailVerificationMap.get(request.getEmail());

        if(sentVerificationCode == null) {
            throw new AppException(ErrorCode.EMAIL_INVALID);
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.EMAIL_NOT_FOUND));

        if(user.getVerificationExpiration().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.CODE_EXPIRED);
        }
        if(!user.getVerificationCode().equals(request.getVerificationCode())) {
            throw new AppException(ErrorCode.INVALID_CODE);
        }

        if(!Objects.equals(request.getConfirmPassword(), request.getNewPassword())) {
            throw new AppException(ErrorCode.PASSWORD_MISMATCH);
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        user.setVerificationCode(null);
        user.setVerificationExpiration(null);
        user = userRepository.save(user);

        emailVerificationMap.remove(request.getEmail());

        return userMapper.toUserResponse(user);
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

package vdtry06.springboot.ecommerce.service;

import java.util.List;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vdtry06.springboot.ecommerce.dto.request.UserUpdationRequest;
import vdtry06.springboot.ecommerce.dto.response.UserResponse;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.mapper.UserMapper;
import vdtry06.springboot.ecommerce.repository.RoleRepository;
import vdtry06.springboot.ecommerce.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(UserUpdationRequest request) {
        Long userId = getCurrentUserId();

        log.info("Updating user with id: " + userId);

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        log.info("User updated: " + request.getPassword());

        if(userRepository.existsByPassword(request.getPassword())) throw new AppException(ErrorCode.PASSWORD_EXISTED);

        userMapper.updateUser(user, request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userMapper.toUserResponse(userRepository.save(user));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser() {
        Long userId = getCurrentUserId();
        log.info("Getting user with id: " + userId);
        return userMapper.toUserResponse(
                userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED)));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            log.info("User not existed!");
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }

        log.info("Deleting user {}", id);
        userRepository.deleteById(id);
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserResponse(user);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("Getting current user id {}", user.getId());
        return user.getId();
    }


}

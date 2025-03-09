package vdtry06.springboot.ecommerce.user;

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
import vdtry06.springboot.ecommerce.cloudinary.CloudinaryService;
import vdtry06.springboot.ecommerce.user.dto.UserInfoResponse;
import vdtry06.springboot.ecommerce.user.dto.UserUpdationRequest;
import vdtry06.springboot.ecommerce.user.dto.UserResponse;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserMapper userMapper;
    CloudinaryService cloudinaryService;

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse updateUser(Long userId, UserUpdationRequest request) {

        User user = userRepository.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        if(request.getPassword() != null && !request.getPassword().equals(user.getPassword())) {
            if(userRepository.existsByPassword(request.getPassword())) {
                throw new AppException(ErrorCode.PASSWORD_EXISTED);
            }
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            log.info("update user password {}", request.getPassword());
        }

        if(request.getFirstName() != null && !request.getFirstName().equals(user.getFirstName())) {
            user.setFirstName(request.getFirstName());
            log.info("update user first name {}", request.getFirstName());
        }

        if(request.getLastName() != null && !request.getLastName().equals(user.getLastName())) {
            user.setLastName(request.getLastName());
            log.info("update user last name {}", request.getLastName());
        }

        if(request.getDateOfBirth() != null && !request.getDateOfBirth().equals(user.getDateOfBirth())) {
            user.setDateOfBirth(request.getDateOfBirth());
            log.info("update user dateOfBirth {}", request.getDateOfBirth());
        }

        String imageUrl = null;
        if (request.getFile() != null && !request.getFile().isEmpty()) {
            if (user.getImageUrl() != null) {
                log.info("update user image {}", user.getImageUrl());
                cloudinaryService.deleteFile(user.getImageUrl());
            }
            imageUrl = cloudinaryService.uploadFile(request.getFile(), "E-commerce/users/" + user.getId());
            user.setImageUrl(imageUrl);
            log.info("update user image {}", imageUrl);
        }

        userRepository.save(user);

        return userMapper.toUserResponse(user);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getUsers() {
        log.info("Getting all users");
        return userRepository.findAll().stream().map(userMapper::toUserResponse).toList();
    }

    @PostAuthorize("returnObject.username == authentication.name")
    public UserResponse getUser(Long userId) {
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
    public UserInfoResponse getMyInfo() {
        var context = SecurityContextHolder.getContext();
        String name = context.getAuthentication().getName();

        User user = userRepository.findByUsername(name).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));

        return userMapper.toUserInfoResponse((user));
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        log.info("Getting current user id {}", user.getId());
        return user.getId();
    }
//
//    public Optional<UserResponse> findUserById(Long id) {
//        return userRepository.findUserById(id)
//                .map(userMapper::toUserResponse);
//    }
}

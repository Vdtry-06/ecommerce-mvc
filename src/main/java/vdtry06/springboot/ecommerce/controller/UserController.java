package vdtry06.springboot.ecommerce.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import vdtry06.springboot.ecommerce.service.UserService;
import vdtry06.springboot.ecommerce.dto.response.UserInfoResponse;
import vdtry06.springboot.ecommerce.dto.request.UserUpdationRequest;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.dto.response.UserResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserController {

    UserService userService;

    @PutMapping("/users/{userId}")
    public ApiResponse<UserResponse> updateUser(@PathVariable Long userId, @ModelAttribute @Valid UserUpdationRequest request){
        UserResponse userResponse = userService.updateUser(userId, request);
        return ApiResponse.<UserResponse>builder()
                .data(userResponse)
                .message("User updated successfully")
                .build();
    }

    @GetMapping("/users/all")
    public ApiResponse<List<UserResponse>> getUsers() {
        List<UserResponse> users = userService.getUsers();
        return ApiResponse.<List<UserResponse>>builder()
                .data(users)
                .message("Fetched all users successfully")
                .build();
    }

    @GetMapping("/users/{userId}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long userId) {
        UserResponse userResponse = userService.getUser(userId);
        return ApiResponse.<UserResponse>builder()
                .data(userResponse)
                .message("Fetched user successfully")
                .build();
    }

    @DeleteMapping("/users/{userId}")
    public ApiResponse<String> deleteUser(@PathVariable Long userId) {
        userService.deleteUser(userId);
        return ApiResponse.<String>builder()
                .data(String.format("User %s has been deleted!", userId))
                .message("User deleted successfully")
                .build();
    }

    @GetMapping("/users/myInfo")
    public ApiResponse<UserInfoResponse> getMyInfo() {
        UserInfoResponse userInfoResponse = userService.getMyInfo();
        return ApiResponse.<UserInfoResponse>builder()
                .data(userInfoResponse)
                .message("Fetched user information successfully")
                .build();
    }

    @GetMapping("/users/{userId}/info")
    public ApiResponse<UserInfoResponse> getUserInfoById(@PathVariable Long userId) {
        UserInfoResponse userInfo = userService.getUserInfoById(userId);
        return ApiResponse.<UserInfoResponse>builder()
                .data(userInfo)
                .message("Fetched user information successfully")
                .build();
    }
}

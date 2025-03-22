package vdtry06.springboot.ecommerce.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vdtry06.springboot.ecommerce.user.dto.*;
import vdtry06.springboot.ecommerce.user.password.SendEmailRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterUserRequest request);

    User toUserSendEmail(SendEmailRequest request);

    UserResponse toUserResponse(User user);


    UserInfoResponse toUserInfoResponse(User user);

    RegisterUserResponse toRegisterUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdationRequest request);
}

package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vdtry06.springboot.ecommerce.dto.request.RegisterUserRequest;
import vdtry06.springboot.ecommerce.dto.request.UserUpdationRequest;
import vdtry06.springboot.ecommerce.dto.response.RegisterUserResponse;
import vdtry06.springboot.ecommerce.dto.response.UserInfoResponse;
import vdtry06.springboot.ecommerce.dto.response.UserResponse;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.dto.request.SendEmailRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterUserRequest request);

    User toUserSendEmail(SendEmailRequest request);

    @Mapping(source = "role", target = "role")
    UserResponse toUserResponse(User user);

    @Mapping(source = "role", target = "role")
    UserInfoResponse toUserInfoResponse(User user);

    RegisterUserResponse toRegisterUserResponse(User user);

    @Mapping(target = "role", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdationRequest request);
}

package vdtry06.springboot.ecommerce.user;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import vdtry06.springboot.ecommerce.user.dto.RegisterUserRequest;
import vdtry06.springboot.ecommerce.user.dto.UserUpdationRequest;
import vdtry06.springboot.ecommerce.user.password.SendEmailRequest;
import vdtry06.springboot.ecommerce.user.dto.RegisterUserResponse;
import vdtry06.springboot.ecommerce.user.dto.UserResponse;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterUserRequest request);

    User toUserSendEmail(SendEmailRequest request);

    UserResponse toUserResponse(User user);

    RegisterUserResponse toRegisterUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdationRequest request);
}

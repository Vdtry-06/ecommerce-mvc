package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vdtry06.springboot.ecommerce.dto.request.user.RegisterUserRequest;
import vdtry06.springboot.ecommerce.dto.request.user.UserUpdationRequest;
import vdtry06.springboot.ecommerce.dto.request.password.SendEmailRequest;
import vdtry06.springboot.ecommerce.dto.response.user.RegisterUserResponse;
import vdtry06.springboot.ecommerce.dto.response.user.UserResponse;
import vdtry06.springboot.ecommerce.entity.User;

import java.util.Optional;

@Mapper(componentModel = "spring")
public interface UserMapper {
    User toUser(RegisterUserRequest request);

    User toUserSendEmail(SendEmailRequest request);

    UserResponse toUserResponse(User user);

    RegisterUserResponse toRegisterUserResponse(User user);

    @Mapping(target = "roles", ignore = true)
    void updateUser(@MappingTarget User user, UserUpdationRequest request);
}

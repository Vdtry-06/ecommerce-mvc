package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vdtry06.springboot.ecommerce.entity.Role;
import vdtry06.springboot.ecommerce.dto.request.RoleRequest;
import vdtry06.springboot.ecommerce.dto.response.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}

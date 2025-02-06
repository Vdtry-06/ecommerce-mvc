package vdtry06.springboot.ecommerce.role;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import vdtry06.springboot.ecommerce.role.dto.RoleRequest;
import vdtry06.springboot.ecommerce.role.dto.RoleResponse;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", ignore = true)
    Role toRole(RoleRequest request);

    RoleResponse toRoleResponse(Role role);
}

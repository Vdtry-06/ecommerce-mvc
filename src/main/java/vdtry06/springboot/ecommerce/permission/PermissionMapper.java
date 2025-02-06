package vdtry06.springboot.ecommerce.permission;

import org.mapstruct.Mapper;

import vdtry06.springboot.ecommerce.permission.dto.PermissionRequest;
import vdtry06.springboot.ecommerce.permission.dto.PermissionResponse;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}

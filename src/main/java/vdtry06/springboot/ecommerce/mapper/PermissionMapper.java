package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;

import vdtry06.springboot.ecommerce.dto.request.PermissionRequest;
import vdtry06.springboot.ecommerce.dto.response.PermissionResponse;
import vdtry06.springboot.ecommerce.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    Permission toPermission(PermissionRequest request);

    PermissionResponse toPermissionResponse(Permission permission);
}

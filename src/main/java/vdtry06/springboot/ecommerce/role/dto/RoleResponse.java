package vdtry06.springboot.ecommerce.role.dto;

import java.util.Set;

import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.permission.dto.PermissionResponse;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleResponse {
    String name;
    String description;
    Set<PermissionResponse> permissions;
}

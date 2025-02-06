package vdtry06.springboot.ecommerce.role;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import vdtry06.springboot.ecommerce.permission.Permission;
import vdtry06.springboot.ecommerce.user.User;

import java.util.Set;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    String name;

    String description;

    @ManyToMany
    @JoinTable(
            name = "role_permission",
            joinColumns = @JoinColumn(name = "role_name"),
            inverseJoinColumns = @JoinColumn(name = "permission_id"))
    Set<Permission> permissions;

    @ManyToMany(mappedBy = "roles")
    Set<User> users;
}

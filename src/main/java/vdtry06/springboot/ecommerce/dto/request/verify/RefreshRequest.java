package vdtry06.springboot.ecommerce.dto.request.verify;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshRequest {
    String token;
}

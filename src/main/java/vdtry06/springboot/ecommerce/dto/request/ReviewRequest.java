package vdtry06.springboot.ecommerce.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewRequest {
    Long productId;
    Long userId;
    Long ratingScore;
    String comment;
}

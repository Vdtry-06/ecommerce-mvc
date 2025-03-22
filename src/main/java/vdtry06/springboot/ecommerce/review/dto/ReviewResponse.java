package vdtry06.springboot.ecommerce.review.dto;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ReviewResponse {
    Long id;
    Long productId;
    Long userId;
    Long ratingScore;
    String comment;
    LocalDateTime reviewDate;
}

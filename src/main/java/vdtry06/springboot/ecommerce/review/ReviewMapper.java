package vdtry06.springboot.ecommerce.review;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import vdtry06.springboot.ecommerce.review.dto.ReviewRequest;
import vdtry06.springboot.ecommerce.review.dto.ReviewResponse;

@Mapper(componentModel = "spring")
public interface ReviewMapper {

    Review toReview(ReviewRequest review);

    default ReviewResponse toReviewResponse(Review review) {
        return ReviewResponse.builder()
                .id(review.getId())
                .productId(review.getProduct() != null ? review.getProduct().getId() : null)
                .userId(review.getUser() != null ? review.getUser().getId() : null)
                .ratingScore(review.getRatingScore())
                .comment(review.getComment())
                .reviewDate(review.getReviewDate())
                .build();
    }
}

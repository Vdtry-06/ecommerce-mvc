package vdtry06.springboot.ecommerce.mapper;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.entity.Review;
import vdtry06.springboot.ecommerce.dto.request.ReviewRequest;
import vdtry06.springboot.ecommerce.dto.response.ReviewResponse;

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
                .visible(review.isVisible())
                .build();
    }
}

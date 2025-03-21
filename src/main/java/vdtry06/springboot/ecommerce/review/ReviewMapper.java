package vdtry06.springboot.ecommerce.review;

import org.mapstruct.Mapper;
import vdtry06.springboot.ecommerce.review.dto.ReviewRequest;
import vdtry06.springboot.ecommerce.review.dto.ReviewResponse;

@Mapper(componentModel = "spring")
public interface ReviewMapper {
    Review toReview(ReviewRequest review);

    ReviewResponse toReviewResponse(Review review);
}

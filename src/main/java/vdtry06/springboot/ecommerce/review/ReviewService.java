package vdtry06.springboot.ecommerce.review;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.core.exception.AppException;
import vdtry06.springboot.ecommerce.core.exception.ErrorCode;
import vdtry06.springboot.ecommerce.review.dto.ReviewRequest;
import vdtry06.springboot.ecommerce.review.dto.ReviewResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewService {
    ReviewRepository reviewRepository;
    ReviewMapper reviewMapper;

    public ReviewResponse addReview(ReviewRequest request) {

        Review review = reviewMapper.toReview(request);
        review.setRatingScore(request.getRatingScore());
        review.setComment(request.getComment());
        review.setReviewDate(LocalDateTime.now());
        reviewRepository.save(review);
        return reviewMapper.toReviewResponse(review);
    }

    public ReviewResponse updateReview(Long id, ReviewRequest request) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        if(request.getRatingScore() != null) {
            review.setRatingScore(request.getRatingScore());
        }

        if(request.getComment() != null && !request.getComment().isEmpty()) {
            review.setComment(request.getComment());
        }
        if(request.getComment() != null || request.getRatingScore() != null) {
            review.setReviewDate(LocalDateTime.now());
        }
        reviewRepository.save(review);

        return  reviewMapper.toReviewResponse(review);
    }

    public ReviewResponse getReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        return reviewMapper.toReviewResponse(review);
    }

    public List<ReviewResponse> getAllReviews() {
        return reviewRepository.findAll().stream()
                .map(reviewMapper::toReviewResponse)
                .collect(Collectors.toList());
    }

    public void deleteReview(Long id) {
        if(!reviewRepository.existsById(id)) {
            throw new AppException(ErrorCode.REVIEW_NOT_EXISTED);
        }
        reviewRepository.deleteById(id);
    }
}

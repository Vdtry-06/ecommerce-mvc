package vdtry06.springboot.ecommerce.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.exception.AppException;
import vdtry06.springboot.ecommerce.exception.ErrorCode;
import vdtry06.springboot.ecommerce.entity.Product;
import vdtry06.springboot.ecommerce.entity.Review;
import vdtry06.springboot.ecommerce.mapper.ReviewMapper;
import vdtry06.springboot.ecommerce.repository.ProductRepository;
import vdtry06.springboot.ecommerce.repository.ReviewRepository;
import vdtry06.springboot.ecommerce.dto.request.ReviewRequest;
import vdtry06.springboot.ecommerce.dto.response.ReviewResponse;
import vdtry06.springboot.ecommerce.entity.User;
import vdtry06.springboot.ecommerce.repository.UserRepository;

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
    ProductRepository productRepository;
    UserRepository userRepository;

    public ReviewResponse addReview(ReviewRequest request) {
        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
        boolean alreadyReviewed = reviewRepository.existsByUserAndProduct(user, product);
        if (alreadyReviewed) {
            throw new AppException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }
        Review review = reviewMapper.toReview(request);
        review.setProduct(product);
        review.setUser(user);
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

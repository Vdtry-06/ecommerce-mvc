package vdtry06.springboot.ecommerce.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import vdtry06.springboot.ecommerce.dto.ApiResponse;
import vdtry06.springboot.ecommerce.service.ReviewService;
import vdtry06.springboot.ecommerce.dto.request.ReviewRequest;
import vdtry06.springboot.ecommerce.dto.response.ReviewResponse;

import java.util.List;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class ReviewController {
    ReviewService reviewService;

    @PostMapping("/add")
    public ApiResponse<ReviewResponse> addReview(@RequestBody ReviewRequest reviewRequest) {
        ReviewResponse reviewResponse = reviewService.addReview(reviewRequest);
        return ApiResponse.<ReviewResponse>builder()
                .data(reviewResponse)
                .build();
    }

    @PutMapping("/update/{id}")
    public ApiResponse<ReviewResponse> updateReview(@PathVariable Long id , @RequestBody ReviewRequest reviewRequest) {
        ReviewResponse reviewResponse = reviewService.updateReview(id, reviewRequest);
        return ApiResponse.<ReviewResponse>builder()
                .data(reviewResponse)
                .build();
    }

    @PostMapping("/delete/{id}")
    public ApiResponse<String> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ApiResponse.<String>builder()
                .data(String.format("Review with id %s deleted", id))
                .build();
    }

    @GetMapping("/get/{id}")
    public ApiResponse<ReviewResponse> getReview(@PathVariable Long id) {
        ReviewResponse reviewResponse = reviewService.getReview(id);
        return ApiResponse.<ReviewResponse>builder()
                .data(reviewResponse)
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<ReviewResponse>> getAllReviews() {
        List<ReviewResponse> reviewResponses = reviewService.getAllReviews();
        return ApiResponse.<List<ReviewResponse>>builder()
                .data(reviewResponses)
                .build();
    }
}

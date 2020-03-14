package org.flowable.eventdemo.review;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import reactor.core.publisher.Mono;

@RestController
public class ReviewController {

    private ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping(value = "/reviews")
    public Mono<ResponseEntity<Void>> processReview(@RequestBody String review) {
        return reviewService.sendReviewToKafka(review)
            .then(Mono.just(ResponseEntity.ok().build()));
    }

}

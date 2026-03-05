package com.study.ngy.web;

import com.study.ngy.domain.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/gallery/{id}/review")
    public String addReview(@PathVariable Long id,
                            @RequestParam String authorName,
                            @RequestParam int rating,
                            @RequestParam String content) {
        reviewService.addReview(id, authorName, rating, content);
        return "redirect:/gallery/" + id + "?reviewSubmitted=true";
    }
}

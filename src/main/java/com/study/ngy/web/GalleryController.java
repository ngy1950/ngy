package com.study.ngy.web;

import com.study.ngy.domain.gallery.GalleryPost;
import com.study.ngy.domain.gallery.GalleryService;
import com.study.ngy.domain.review.Review;
import com.study.ngy.domain.review.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;
    private final ReviewService reviewService;

    @GetMapping("/gallery")
    public String list(@RequestParam(required = false) String category, Model model) {
        List<GalleryPost> posts;
        if (category != null && !category.isBlank()) {
            posts = galleryService.findByCategory(category);
            model.addAttribute("selectedCategory", category);
        } else {
            posts = galleryService.findAll();
            model.addAttribute("selectedCategory", "all");
        }
        model.addAttribute("posts", posts);
        return "menu/gallery";
    }

    @GetMapping("/gallery/{id}")
    public String detail(@PathVariable Long id, Model model) {
        GalleryPost post = galleryService.findById(id);
        List<Review> reviews = reviewService.findApprovedByPostId(id);
        model.addAttribute("post", post);
        model.addAttribute("reviews", reviews);
        return "menu/gallery-detail";
    }
}

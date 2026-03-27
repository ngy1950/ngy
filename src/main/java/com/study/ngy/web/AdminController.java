package com.study.ngy.web;

import com.study.ngy.domain.gallery.GalleryService;
import com.study.ngy.domain.review.ReviewService;
import com.study.ngy.domain.visitor.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final GalleryService galleryService;
    private final ReviewService reviewService;
    private final VisitorService visitorService;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model,
                            @PageableDefault(size = 20, sort = "visitedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        model.addAttribute("posts", galleryService.findAll());
        model.addAttribute("pendingReviews", reviewService.findPendingReviews());
        model.addAttribute("visitorStats", visitorService.getRecentStats());
        model.addAttribute("recentRawLogs", visitorService.getRecentRawLogs(pageable));
        model.addAttribute("deviceStats", visitorService.getDeviceStats());
        model.addAttribute("referrerStats", visitorService.getReferrerStats());
        model.addAttribute("ipStats", visitorService.getIpStats());
        model.addAttribute("recentSessionIds", visitorService.getRecentSessionIds());
        model.addAttribute("uniqueVisitorCounts", visitorService.getUniqueVisitorCounts());
        return "admin/dashboard";
    }

    @GetMapping("/visitor/session/{sessionId}")
    @ResponseBody
    public java.util.List<java.util.Map<String, Object>> sessionFlow(@PathVariable String sessionId) {
        return visitorService.getSessionFlow(sessionId).stream()
                .<java.util.Map<String, Object>>map(log -> {
                    java.util.Map<String, Object> map = new java.util.LinkedHashMap<>();
                    map.put("time", log.getVisitedAt().toString());
                    map.put("page", log.getPage());
                    map.put("device", log.getDeviceType());
                    return map;
                })
                .toList();
    }

    @GetMapping("/gallery/new")
    public String uploadForm() {
        return "admin/upload";
    }

    @PostMapping("/gallery/new")
    public String uploadPost(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String category,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                             @RequestParam("images") List<MultipartFile> images) throws IOException {
        galleryService.create(title, description, category, eventDate, images);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/gallery/{id}/delete")
    public String deletePost(@PathVariable Long id) {
        galleryService.delete(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/review/{id}/approve")
    public String approveReview(@PathVariable Long id) {
        reviewService.approve(id);
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/review/{id}/delete")
    public String deleteReview(@PathVariable Long id) {
        reviewService.delete(id);
        return "redirect:/admin/dashboard";
    }
}

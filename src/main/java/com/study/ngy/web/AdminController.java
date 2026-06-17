package com.study.ngy.web;

import com.study.ngy.domain.cta.CtaService;
import com.study.ngy.domain.event.EventService;
import com.study.ngy.domain.gallery.GalleryService;
import com.study.ngy.domain.order.Order;
import com.study.ngy.domain.order.OrderService;
import com.study.ngy.domain.review.ReviewService;
import com.study.ngy.domain.scroll.ScrollService;
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
    private final CtaService ctaService;
    private final EventService eventService;
    private final ScrollService scrollService;
    private final OrderService orderService;

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
        model.addAttribute("ctaStats", ctaService.getCtaStats());
        model.addAttribute("dwellTimeStats", visitorService.getDwellTimeStats());
        model.addAttribute("bounceRate", visitorService.getBounceRateByPage());
        java.util.Map<String, Long> funnel = visitorService.getFunnelStats();
        funnel.put("메뉴 카드 클릭", eventService.countMenuClickSessions());
        funnel.put("CTA 클릭", ctaService.getTotalCtaCount());
        model.addAttribute("funnelStats", funnel);
        model.addAttribute("exitPageStats",  visitorService.getExitPageStats());
        model.addAttribute("scrollStats",    scrollService.getScrollStats());
        model.addAttribute("eventStats",     eventService.getEventStats());
        model.addAttribute("utmStats",       visitorService.getUtmStats());
        model.addAttribute("sessionStats",   visitorService.getSessionStats());
        return "admin/dashboard";
    }

    @GetMapping("/visitor/chart")
    @ResponseBody
    public java.util.Map<String, Object> referrerChart(
            @RequestParam(defaultValue = "daily") String period) {
        return visitorService.getReferrerChartData(period);
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

    @GetMapping("/gallery/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("post", galleryService.findById(id));
        return "admin/edit";
    }

    @PostMapping("/gallery/{id}/edit")
    public String updatePost(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String category,
                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate eventDate,
                             @RequestParam(value = "images", required = false) List<MultipartFile> images) throws IOException {
        galleryService.update(id, title, description, category, eventDate, images);
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

    // ── 주문 관리 ──────────────────────────────────────────

    @GetMapping("/orders")
    public String orderList(Model model) {
        model.addAttribute("orders", orderService.findAll());
        model.addAttribute("today", java.time.LocalDate.now());
        return "admin/orders";
    }

    @GetMapping("/orders/new")
    public String orderForm(Model model) {
        model.addAttribute("order", new Order());
        model.addAttribute("editMode", false);
        return "admin/order-form";
    }

    @PostMapping("/orders/new")
    public String orderCreate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate,
            @RequestParam(required = false) String menuDescription,
            @RequestParam(defaultValue = "false") boolean paid,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam String recipientName,
            @RequestParam String recipientPhone,
            @RequestParam(required = false) String recipientAddress,
            @RequestParam(required = false) String memo) {
        orderService.create(deliveryDate, menuDescription, paid, trackingNumber,
                recipientName, recipientPhone, recipientAddress, memo);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/{id}/edit")
    public String orderEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findById(id));
        model.addAttribute("editMode", true);
        return "admin/order-form";
    }

    @PostMapping("/orders/{id}/edit")
    public String orderUpdate(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deliveryDate,
            @RequestParam(required = false) String menuDescription,
            @RequestParam(defaultValue = "false") boolean paid,
            @RequestParam(required = false) String trackingNumber,
            @RequestParam String recipientName,
            @RequestParam String recipientPhone,
            @RequestParam(required = false) String recipientAddress,
            @RequestParam(required = false) String memo) {
        orderService.update(id, deliveryDate, menuDescription, paid, trackingNumber,
                recipientName, recipientPhone, recipientAddress, memo);
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/{id}/paid")
    public String togglePaid(@PathVariable Long id) {
        orderService.togglePaid(id);
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/{id}/delete")
    public String deleteOrder(@PathVariable Long id) {
        orderService.delete(id);
        return "redirect:/admin/orders";
    }
}

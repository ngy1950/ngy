package com.study.ngy.web;

import com.study.ngy.domain.cta.CtaService;
import com.study.ngy.domain.event.EventService;
import com.study.ngy.domain.scroll.ScrollService;
import com.study.ngy.domain.visitor.VisitorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TrackingController {

    private final CtaService ctaService;
    private final VisitorService visitorService;
    private final EventService eventService;
    private final ScrollService scrollService;

    /** CTA 클릭 기록 — GET이라 CSRF 불필요, fetch keepalive로 호출됨 */
    @GetMapping("/track/cta")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackCta(@RequestParam String type,
                         @RequestParam(required = false) String page) {
        ctaService.record(type, page);
    }

    /** 페이지 체류 시간 기록 — JS가 페이지 이탈 시 sendBeacon으로 호출 */
    @PostMapping("/track/dwell")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackDwell(@RequestParam String vid,
                           @RequestParam String page,
                           @RequestParam int seconds) {
        if (seconds >= 2 && seconds <= 3600) {
            visitorService.updateDwellTime(vid, page, seconds);
        }
    }

    /** 클릭 이벤트 기록 — 메뉴 카드, 갤러리 카드 등 data-track-action 요소 클릭 시 */
    @GetMapping("/track/event")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackEvent(@RequestParam String action,
                           @RequestParam(required = false) String label,
                           @RequestParam(required = false) String page,
                           @CookieValue(value = "vid", required = false) String vid) {
        eventService.record(action, label, page, vid);
    }

    /** 스크롤 깊이 기록 — 25/50/75/100% 도달 시 fetch keepalive로 호출 */
    @GetMapping("/track/scroll")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackScroll(@RequestParam int depth,
                            @RequestParam(required = false) String page,
                            @CookieValue(value = "vid", required = false) String vid) {
        scrollService.record(vid, page != null ? page : "/", depth);
    }
}

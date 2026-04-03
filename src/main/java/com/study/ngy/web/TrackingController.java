package com.study.ngy.web;

import com.study.ngy.domain.cta.CtaService;
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
}

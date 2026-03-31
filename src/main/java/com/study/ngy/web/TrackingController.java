package com.study.ngy.web;

import com.study.ngy.domain.cta.CtaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TrackingController {

    private final CtaService ctaService;

    /**
     * CTA 클릭 기록 — GET이라 CSRF 불필요, fetch keepalive로 호출됨
     * type: PHONE | KAKAO
     * page: 클릭이 발생한 페이지 경로
     */
    @GetMapping("/track/cta")
    @ResponseBody
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void trackCta(@RequestParam String type,
                         @RequestParam(required = false) String page) {
        ctaService.record(type, page);
    }
}

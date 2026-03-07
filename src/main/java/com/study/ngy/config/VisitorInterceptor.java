package com.study.ngy.config;

import com.study.ngy.domain.visitor.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class VisitorInterceptor implements HandlerInterceptor {

    private final VisitorService visitorService;

    private static final String STATIC_EXT_REGEX =
            ".*\\.(css|js|ico|png|jpg|jpeg|gif|svg|webp|woff|woff2|ttf|eot|map)$";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.matches(STATIC_EXT_REGEX) || uri.startsWith("/admin")) {
            return true;
        }

        String page = normalise(uri);

        // 기존 집계 카운트
        visitorService.recordVisit(page);

        // 원시 로그 — IP / 유입경로 / 기기 / 세션
        String ip = extractIp(request);
        String referrer = request.getHeader("Referer");
        String referrerSource = parseReferrerSource(referrer);
        String deviceType = parseDeviceType(request.getHeader("User-Agent"));
        String sessionId = getSessionId(request);

        visitorService.recordRawVisit(page, ip, referrer, referrerSource, deviceType, sessionId);

        return true;
    }

    /** /gallery/123 → /gallery/{id} */
    private String normalise(String uri) {
        return uri.replaceAll("/\\d+", "/{id}");
    }

    /** Railway 등 프록시 뒤에서 실제 클라이언트 IP 추출 */
    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /** Referer 헤더를 DIRECT / NAVER / GOOGLE / KAKAO / OTHER 로 분류 */
    private String parseReferrerSource(String referrer) {
        if (referrer == null || referrer.isBlank()) return "DIRECT";
        String lower = referrer.toLowerCase();
        if (lower.contains("naver.com")) return "NAVER";
        if (lower.contains("google.")) return "GOOGLE";
        if (lower.contains("kakao.com") || lower.contains("kakaotalk")) return "KAKAO";
        if (lower.contains("instagram.com")) return "INSTAGRAM";
        if (lower.contains("facebook.com") || lower.contains("fb.com")) return "FACEBOOK";
        if (lower.contains("youtube.com")) return "YOUTUBE";
        return "OTHER";
    }

    /** User-Agent → MOBILE / TABLET / DESKTOP / UNKNOWN */
    private String parseDeviceType(String ua) {
        if (ua == null || ua.isBlank()) return "UNKNOWN";
        String lower = ua.toLowerCase();
        if (lower.contains("ipad") || lower.contains("tablet") ||
                (lower.contains("android") && !lower.contains("mobile"))) {
            return "TABLET";
        }
        if (lower.contains("mobile") || lower.contains("iphone") ||
                lower.contains("android") || lower.contains("blackberry")) {
            return "MOBILE";
        }
        return "DESKTOP";
    }

    /** 세션 ID (없으면 새로 생성) */
    private String getSessionId(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        return session.getId();
    }
}

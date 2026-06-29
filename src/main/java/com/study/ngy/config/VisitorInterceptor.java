package com.study.ngy.config;

import com.study.ngy.domain.visitor.VisitorService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class VisitorInterceptor implements HandlerInterceptor {

    private final VisitorService visitorService;

    private static final String STATIC_EXT_REGEX =
            ".*\\.(css|js|ico|png|jpg|jpeg|gif|svg|webp|woff|woff2|ttf|eot|map|xml|txt)$";

    private static final Set<String> CRAWLER_PATHS = Set.of(
            "/sitemap.xml", "/robots.txt", "/favicon.ico"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (uri.matches(STATIC_EXT_REGEX) || uri.startsWith("/admin")
                || CRAWLER_PATHS.contains(uri) || uri.startsWith("/.well-known/")) {
            return true;
        }

        // 크롤러/봇 User-Agent 차단
        String ua = request.getHeader("User-Agent");
        if (ua != null) {
            String uaLower = ua.toLowerCase();
            if (uaLower.contains("bot") || uaLower.contains("crawler")
                    || uaLower.contains("spider") || uaLower.contains("slurp")
                    || uaLower.contains("curl") || uaLower.contains("wget")) {
                return true;
            }
        }

        String page = normalise(uri);

        // 원시 로그 — IP / 유입경로 / 기기 / 세션 / UTM
        String ip = extractIp(request);
        String referrer = request.getHeader("Referer");
        String referrerSource = parseReferrerSource(referrer);
        String deviceType = parseDeviceType(request.getHeader("User-Agent"));
        String sessionId = getOrCreateVid(request, response);

        // UTM 파라미터 — URL 우선, 없으면 쿠키에서 읽음
        String utmSource   = firstNonBlank(request.getParameter("utm_source"),   getCookie(request, "utm_source"));
        String utmMedium   = firstNonBlank(request.getParameter("utm_medium"),   getCookie(request, "utm_medium"));
        String utmCampaign = firstNonBlank(request.getParameter("utm_campaign"), getCookie(request, "utm_campaign"));

        try {
            // 내부 운영 경로(달력 뷰 등)는 집계 카운트 제외, 원시 로그는 유지
            if (!uri.startsWith("/schedule/")) {
                visitorService.recordVisit(page);
            }
            visitorService.recordRawVisit(page, ip, referrer, referrerSource, deviceType, sessionId,
                    utmSource, utmMedium, utmCampaign);
            visitorService.upsertSession(sessionId);
        } catch (Exception e) {
            log.warn("[VisitorInterceptor] DB 저장 실패, 요청은 계속 진행: {}", e.getMessage());
        }

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

    /** Referer 헤더를 상세 유입 소스로 분류 */
    private String parseReferrerSource(String referrer) {
        if (referrer == null || referrer.isBlank()) return "DIRECT";
        String lower = referrer.toLowerCase();
        // 네이버 세부 채널
        if (lower.contains("place.naver.com"))  return "NAVER_PLACE";   // 스마트플레이스
        if (lower.contains("map.naver.com"))    return "NAVER_MAP";     // 네이버 지도
        if (lower.contains("blog.naver.com"))   return "NAVER_BLOG";    // 네이버 블로그
        if (lower.contains("cafe.naver.com"))   return "NAVER_CAFE";    // 네이버 카페
        if (lower.contains("search.naver.com")) return "NAVER_SEARCH";  // 네이버 검색
        if (lower.contains("naver.com"))        return "NAVER_OTHER";   // 기타 네이버
        // 카카오 세부 채널
        if (lower.contains("map.kakao.com"))    return "KAKAO_MAP";     // 카카오맵
        if (lower.contains("kakaotalk") || lower.contains("kakaolink")) return "KAKAO_TALK"; // 카카오톡
        if (lower.contains("kakao.com"))        return "KAKAO_OTHER";   // 기타 카카오
        // 기타 채널
        if (lower.contains("google."))          return "GOOGLE";
        if (lower.contains("instagram.com"))    return "INSTAGRAM";
        if (lower.contains("facebook.com") || lower.contains("fb.com")) return "FACEBOOK";
        if (lower.contains("youtube.com"))      return "YOUTUBE";
        if (lower.contains("band.us"))          return "BAND";          // 네이버 밴드
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

    private String firstNonBlank(String a, String b) {
        if (a != null && !a.isBlank()) return a;
        if (b != null && !b.isBlank()) return b;
        return null;
    }

    private String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        for (Cookie c : request.getCookies()) {
            if (name.equals(c.getName())) return c.getValue();
        }
        return null;
    }

    /**
     * vid 쿠키 — JS에서도 읽을 수 있는 방문자 추적 UUID.
     * JSESSIONID 대신 별도 쿠키를 사용해 세션 보안과 분리.
     */
    private String getOrCreateVid(HttpServletRequest request, HttpServletResponse response) {
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("vid".equals(c.getName())) return c.getValue();
            }
        }
        String vid = UUID.randomUUID().toString();
        // SameSite=Lax 는 Jakarta Cookie API 미지원 → Set-Cookie 헤더 직접 설정
        response.addHeader("Set-Cookie",
                "vid=" + vid + "; Max-Age=2592000; Path=/; SameSite=Lax");
        return vid;
    }
}

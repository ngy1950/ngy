package com.study.ngy.config;

import com.study.ngy.domain.visitor.VisitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class VisitorInterceptor implements HandlerInterceptor {

    private final VisitorService visitorService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!"GET".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        visitorService.recordVisit(normalise(uri));
        return true;
    }

    /** /gallery/123 → /gallery/{id} 처럼 숫자 경로 변수를 치환해 집계 단순화 */
    private String normalise(String uri) {
        return uri.replaceAll("/\\d+", "/{id}");
    }
}

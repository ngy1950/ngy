package com.study.ngy.domain.cta;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CtaService {

    private final CtaClickRepository ctaClickRepository;

    @Transactional
    public void record(String type, String page) {
        String safeType = "KAKAO".equalsIgnoreCase(type) ? "KAKAO" : "PHONE";
        String safePage = (page == null || page.isBlank()) ? "/" : page.substring(0, Math.min(page.length(), 100));
        ctaClickRepository.save(new CtaClick(safeType, safePage));
    }

    /** 최근 CTA 클릭 20건 (대시보드 로그 표시용) */
    @Transactional(readOnly = true)
    public List<CtaClick> getRecentCtaClicks() {
        return ctaClickRepository.findTop20ByOrderByClickedAtDesc();
    }

    /** 최근 7일 전체 CTA 클릭 수 (퍼널용) */
    @Transactional(readOnly = true)
    public long getTotalCtaCount() {
        return ctaClickRepository.countByClickedAtGreaterThanEqual(
                LocalDateTime.now().minusDays(7));
    }

    /** 오늘 / 7일 / 30일 전화·카카오 클릭 수 */
    @Transactional(readOnly = true)
    public Map<String, Object> getCtaStats() {
        LocalDateTime today   = LocalDate.now().atStartOfDay();
        LocalDateTime week    = LocalDateTime.now().minusDays(7);
        LocalDateTime month   = LocalDateTime.now().minusDays(30);

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("phone_today",   ctaClickRepository.countByTypeAndClickedAtGreaterThanEqual("PHONE", today));
        map.put("phone_7days",   ctaClickRepository.countByTypeAndClickedAtGreaterThanEqual("PHONE", week));
        map.put("phone_30days",  ctaClickRepository.countByTypeAndClickedAtGreaterThanEqual("PHONE", month));
        map.put("kakao_today",   ctaClickRepository.countByTypeAndClickedAtGreaterThanEqual("KAKAO", today));
        map.put("kakao_7days",   ctaClickRepository.countByTypeAndClickedAtGreaterThanEqual("KAKAO", week));
        map.put("kakao_30days",  ctaClickRepository.countByTypeAndClickedAtGreaterThanEqual("KAKAO", month));

        // 페이지별 분석 (최근 7일)
        List<Object[]> rows = ctaClickRepository.countByPageAndTypeSince(week);
        List<Map<String, Object>> byPage = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> entry = new LinkedHashMap<>();
            entry.put("page",  row[0]);
            entry.put("type",  row[1]);
            entry.put("count", ((Number) row[2]).longValue());
            byPage.add(entry);
        }
        map.put("byPage", byPage);
        return map;
    }
}

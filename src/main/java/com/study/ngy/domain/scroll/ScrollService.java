package com.study.ngy.domain.scroll;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ScrollService {

    private final ScrollLogRepository scrollLogRepository;

    @Transactional
    public void record(String sessionId, String page, int depth) {
        if (depth != 25 && depth != 50 && depth != 75 && depth != 100) return;
        scrollLogRepository.save(new ScrollLog(sessionId, page, depth));
    }

    /** 최근 7일 페이지별 스크롤 깊이 도달 통계 */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getScrollStats() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = scrollLogRepository.countByPageAndDepthSince(from);

        Map<String, Map<Integer, Long>> pageMap = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String page = (String) row[0];
            int depth = ((Number) row[1]).intValue();
            long count = ((Number) row[2]).longValue();
            pageMap.computeIfAbsent(page, k -> new LinkedHashMap<>()).put(depth, count);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Map.Entry<String, Map<Integer, Long>> entry : pageMap.entrySet()) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("page", entry.getKey());
            map.put("d25",  entry.getValue().getOrDefault(25,  0L));
            map.put("d50",  entry.getValue().getOrDefault(50,  0L));
            map.put("d75",  entry.getValue().getOrDefault(75,  0L));
            map.put("d100", entry.getValue().getOrDefault(100, 0L));
            result.add(map);
        }
        return result;
    }
}

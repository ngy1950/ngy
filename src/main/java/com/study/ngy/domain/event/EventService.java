package com.study.ngy.domain.event;

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
public class EventService {

    private final EventLogRepository eventLogRepository;

    @Transactional
    public void record(String action, String label, String page, String sessionId) {
        if (action == null || action.isBlank()) return;
        eventLogRepository.save(new EventLog(
                action.substring(0, Math.min(action.length(), 50)),
                label != null ? label.substring(0, Math.min(label.length(), 100)) : null,
                page != null ? page.substring(0, Math.min(page.length(), 100)) : "/",
                sessionId
        ));
    }

    /** 최근 7일 action·label별 클릭 집계 */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getEventStats() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = eventLogRepository.countByActionAndLabelSince(from);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("action", row[0]);
            map.put("label", row[1]);
            map.put("count", ((Number) row[2]).longValue());
            result.add(map);
        }
        return result;
    }

    /** 퍼널용 — 메뉴 카드 클릭 순 세션 수 (최근 7일) */
    @Transactional(readOnly = true)
    public Long countMenuClickSessions() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        return eventLogRepository.countDistinctSessionsByActionSince("menu_click", from);
    }
}

package com.study.ngy.domain.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorLogRepository visitorLogRepository;
    private final VisitorRawLogRepository visitorRawLogRepository;

    /** 기존 집계 카운트 업데이트 */
    @Transactional
    public void recordVisit(String page) {
        LocalDate today = LocalDate.now();
        visitorLogRepository.findByDateAndPage(today, page)
                .ifPresentOrElse(
                        VisitorLog::increment,
                        () -> visitorLogRepository.save(new VisitorLog(today, page))
                );
    }

    /** 원시 로그 저장 (IP/유입경로/기기/세션) */
    @Transactional
    public void recordRawVisit(String page, String ip, String referrer,
                               String referrerSource, String deviceType, String sessionId) {
        visitorRawLogRepository.save(
                new VisitorRawLog(page, ip, referrer, referrerSource, deviceType, sessionId)
        );
    }

    // ─── 집계 통계 ───────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<VisitorLog> getRecentStats() {
        LocalDate from = LocalDate.now().minusDays(13);
        return visitorLogRepository.findByDateGreaterThanEqualOrderByDateDesc(from);
    }

    /** 최근 방문 로그 100건 */
    @Transactional(readOnly = true)
    public List<VisitorRawLog> getRecentRawLogs() {
        return visitorRawLogRepository.findTop100ByOrderByVisitedAtDesc();
    }

    /** 최근 7일 기기 유형별 비율 */
    @Transactional(readOnly = true)
    public Map<String, Long> getDeviceStats() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = visitorRawLogRepository.countByDeviceTypeSince(from);
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    /** 최근 7일 유입 소스별 카운트 */
    @Transactional(readOnly = true)
    public Map<String, Long> getReferrerStats() {
        LocalDateTime from = LocalDateTime.now().minusDays(7);
        List<Object[]> rows = visitorRawLogRepository.countByReferrerSourceSince(from);
        Map<String, Long> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    /** 최근 24시간 IP별 방문 횟수 (상위 20) */
    @Transactional(readOnly = true)
    public Map<String, Long> getIpStats() {
        LocalDateTime from = LocalDateTime.now().minusHours(24);
        List<Object[]> rows = visitorRawLogRepository.countByIpSince(from);
        Map<String, Long> map = new LinkedHashMap<>();
        int limit = 20;
        for (Object[] row : rows) {
            if (limit-- <= 0) break;
            map.put((String) row[0], (Long) row[1]);
        }
        return map;
    }

    /** 특정 세션의 페이지 이동 흐름 */
    @Transactional(readOnly = true)
    public List<VisitorRawLog> getSessionFlow(String sessionId) {
        return visitorRawLogRepository.findSessionFlow(sessionId);
    }

    /** 최근 24시간 내 세션 목록 */
    @Transactional(readOnly = true)
    public List<String> getRecentSessionIds() {
        LocalDateTime from = LocalDateTime.now().minusHours(24);
        return visitorRawLogRepository.findRecentSessionIds(from);
    }

    /** 순 방문자 수 (오늘 / 최근 7일 / 최근 30일) — IP 기준 중복 제거 */
    @Transactional(readOnly = true)
    public Map<String, Long> getUniqueVisitorCounts() {
        Map<String, Long> map = new LinkedHashMap<>();
        map.put("오늘", visitorRawLogRepository.countDistinctIpSince(
                LocalDate.now().atStartOfDay()));
        map.put("7일", visitorRawLogRepository.countDistinctIpSince(
                LocalDateTime.now().minusDays(7)));
        map.put("30일", visitorRawLogRepository.countDistinctIpSince(
                LocalDateTime.now().minusDays(30)));
        return map;
    }
}

package com.study.ngy.domain.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;;

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

    /** 최근 방문 로그 페이징 처리 (IP별 최신 로그 1건) */
    @Transactional(readOnly = true)
    public Page<VisitorRawLog> getRecentRawLogs(Pageable pageable) {
        return visitorRawLogRepository.findLatestLogsPerIp(pageable);
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

    /** 유입소스 일별 집계 → 일별/주별/월별 차트 데이터 반환 */
    @Transactional(readOnly = true)
    public Map<String, Object> getReferrerChartData(String period) {
        LocalDateTime from = switch (period) {
            case "weekly"  -> LocalDateTime.now().minusWeeks(8);
            case "monthly" -> LocalDateTime.now().minusMonths(6);
            default        -> LocalDateTime.now().minusDays(13);
        };

        List<Object[]> rows = visitorRawLogRepository.findDailyReferrerStats(from);

        // bucket key → (source → count)
        Map<String, Map<String, Long>> buckets = new LinkedHashMap<>();
        Set<String> sources = new LinkedHashSet<>();

        for (Object[] row : rows) {
            LocalDate date = toLocalDate(row[0]);
            String source = (String) row[1];
            long count = ((Number) row[2]).longValue();

            String label = switch (period) {
                case "weekly"  -> date.with(DayOfWeek.MONDAY)
                                      .format(DateTimeFormatter.ofPattern("MM-dd"));
                case "monthly" -> date.format(DateTimeFormatter.ofPattern("yyyy-MM"));
                default        -> date.format(DateTimeFormatter.ofPattern("MM-dd"));
            };

            buckets.computeIfAbsent(label, k -> new LinkedHashMap<>())
                   .merge(source, count, Long::sum);
            sources.add(source);
        }

        List<String> labels = new ArrayList<>(buckets.keySet());
        Map<String, List<Long>> datasets = new LinkedHashMap<>();
        for (String src : sources) {
            List<Long> data = new ArrayList<>();
            for (String label : labels) {
                data.add(buckets.getOrDefault(label, Map.of()).getOrDefault(src, 0L));
            }
            datasets.put(src, data);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("datasets", datasets);
        return result;
    }

    private LocalDate toLocalDate(Object o) {
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof LocalDate d)    return d;
        return LocalDate.parse(o.toString());
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

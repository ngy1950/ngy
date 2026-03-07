package com.study.ngy.domain.visitor;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final VisitorLogRepository visitorLogRepository;

    @Transactional
    public void recordVisit(String page) {
        LocalDate today = LocalDate.now();
        visitorLogRepository.findByDateAndPage(today, page)
                .ifPresentOrElse(
                        VisitorLog::increment,
                        () -> visitorLogRepository.save(new VisitorLog(today, page))
                );
    }

    @Transactional(readOnly = true)
    public List<VisitorLog> getRecentStats() {
        LocalDate from = LocalDate.now().minusDays(13); // 오늘 포함 14일
        return visitorLogRepository.findByDateGreaterThanEqualOrderByDateDesc(from);
    }
}

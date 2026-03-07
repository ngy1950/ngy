package com.study.ngy.domain.visitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface VisitorLogRepository extends JpaRepository<VisitorLog, Long> {

    Optional<VisitorLog> findByDateAndPage(LocalDate date, String page);

    List<VisitorLog> findByDateGreaterThanEqualOrderByDateDesc(LocalDate from);
}

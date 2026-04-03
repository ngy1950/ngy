package com.study.ngy.domain.visitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VisitorSessionRepository extends JpaRepository<VisitorSession, String> {

    List<VisitorSession> findByFirstVisitAtAfter(LocalDateTime from);
}

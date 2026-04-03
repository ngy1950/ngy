package com.study.ngy.domain.visitor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_session")
@Getter
@NoArgsConstructor
public class VisitorSession {

    @Id
    @Column(length = 100)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime firstVisitAt;

    @Column(nullable = false)
    private LocalDateTime lastActivityAt;

    /** 세션 내 방문 페이지 수 */
    @Column(nullable = false)
    private int totalPages;

    public VisitorSession(String sessionId) {
        this.sessionId = sessionId;
        this.firstVisitAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.totalPages = 1;
    }

    public void touch() {
        this.lastActivityAt = LocalDateTime.now();
        this.totalPages++;
    }
}

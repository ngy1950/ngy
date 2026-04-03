package com.study.ngy.domain.scroll;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "scroll_log", indexes = {
        @Index(name = "idx_scroll_recorded_at", columnList = "recordedAt")
})
@Getter
@NoArgsConstructor
public class ScrollLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String sessionId;

    @Column(nullable = false, length = 50)
    private String page;

    /** 25 / 50 / 75 / 100 */
    @Column(nullable = false)
    private int depth;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    public ScrollLog(String sessionId, String page, int depth) {
        this.sessionId = sessionId;
        this.page = page;
        this.depth = depth;
        this.recordedAt = LocalDateTime.now();
    }
}

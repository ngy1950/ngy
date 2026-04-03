package com.study.ngy.domain.event;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "event_log", indexes = {
        @Index(name = "idx_event_recorded_at", columnList = "recordedAt"),
        @Index(name = "idx_event_action", columnList = "action")
})
@Getter
@NoArgsConstructor
public class EventLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** menu_click / gallery_click 등 */
    @Column(nullable = false, length = 50)
    private String action;

    /** jesa / jangji / group / gallery-id 등 */
    @Column(length = 100)
    private String label;

    /** 이벤트 발생 페이지 */
    @Column(length = 100)
    private String page;

    @Column(length = 100)
    private String sessionId;

    @Column(nullable = false)
    private LocalDateTime recordedAt;

    public EventLog(String action, String label, String page, String sessionId) {
        this.action = action;
        this.label = label;
        this.page = page;
        this.sessionId = sessionId;
        this.recordedAt = LocalDateTime.now();
    }
}

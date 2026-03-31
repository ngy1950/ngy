package com.study.ngy.domain.cta;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "cta_click", indexes = {
        @Index(name = "idx_cta_clicked_at", columnList = "clickedAt"),
        @Index(name = "idx_cta_type", columnList = "type")
})
@Getter
@NoArgsConstructor
public class CtaClick {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** PHONE / KAKAO */
    @Column(nullable = false, length = 10)
    private String type;

    /** 클릭이 발생한 페이지 경로 */
    @Column(nullable = false, length = 100)
    private String page;

    @Column(nullable = false)
    private LocalDateTime clickedAt;

    public CtaClick(String type, String page) {
        this.type = type;
        this.page = page;
        this.clickedAt = LocalDateTime.now();
    }
}

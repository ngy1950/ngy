package com.study.ngy.domain.visitor;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "visitor_raw_log", indexes = {
        @Index(name = "idx_raw_visited_at", columnList = "visitedAt"),
        @Index(name = "idx_raw_session", columnList = "sessionId")
})
@Getter
@NoArgsConstructor
public class VisitorRawLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime visitedAt;

    @Column(nullable = false, length = 50)
    private String page;

    @Column(length = 50)
    private String ip;

    /** HTTP Referer 헤더 — 어디서 유입됐는지 */
    @Column(length = 500)
    private String referrer;

    /** 파싱된 유입 소스 (DIRECT / NAVER / GOOGLE / KAKAO / OTHER) */
    @Column(length = 20)
    private String referrerSource;

    /** MOBILE / TABLET / DESKTOP / UNKNOWN */
    @Column(nullable = false, length = 10)
    private String deviceType;

    /** 같은 방문자 세션 내 행동 흐름 추적용 */
    @Column(length = 100)
    private String sessionId;

    /** 페이지 체류 시간 (초). JS가 이탈 시 전송. null = 미수집 */
    @Column(name = "dwell_seconds")
    private Integer dwellSeconds;

    /** 이탈 시각 — visitedAt + dwellSeconds 로 계산. null = 미수집 */
    @Column
    private LocalDateTime exitedAt;

    /** UTM 파라미터 — 광고 유입 추적용 */
    @Column(length = 50)
    private String utmSource;

    @Column(length = 50)
    private String utmMedium;

    @Column(length = 100)
    private String utmCampaign;

    public VisitorRawLog(String page, String ip, String referrer, String referrerSource,
                         String deviceType, String sessionId,
                         String utmSource, String utmMedium, String utmCampaign) {
        this.visitedAt = LocalDateTime.now();
        this.page = page;
        this.ip = ip;
        this.referrer = referrer;
        this.referrerSource = referrerSource;
        this.deviceType = deviceType;
        this.sessionId = sessionId;
        this.utmSource = utmSource;
        this.utmMedium = utmMedium;
        this.utmCampaign = utmCampaign;
    }

    public void updateDwellSeconds(int seconds) {
        this.dwellSeconds = seconds;
        this.exitedAt = this.visitedAt.plusSeconds(seconds);
    }
}

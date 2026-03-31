package com.study.ngy.domain.cta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface CtaClickRepository extends JpaRepository<CtaClick, Long> {

    /** 기간 내 특정 타입 클릭 수 */
    long countByTypeAndClickedAtGreaterThanEqual(String type, LocalDateTime from);

    /** 기간 내 페이지별 타입별 클릭 수 */
    @Query("SELECT c.page, c.type, COUNT(c) FROM CtaClick c " +
           "WHERE c.clickedAt >= :from " +
           "GROUP BY c.page, c.type ORDER BY COUNT(c) DESC")
    List<Object[]> countByPageAndTypeSince(@Param("from") LocalDateTime from);
}

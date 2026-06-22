package com.urlshortener.repository;

import com.urlshortener.model.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ClickRepository extends JpaRepository<ClickEvent, Long> {

    long countByUrlId(Long urlId);

    @Query("""
            SELECT DATE(ce.clickedAt) as day, COUNT(ce) as clicks
            FROM ClickEvent ce
            WHERE ce.url.id = :urlId
              AND ce.clickedAt >= :since
            GROUP BY DATE(ce.clickedAt)
            ORDER BY DATE(ce.clickedAt) DESC
            """)
    List<Object[]> findDailyClicksSince(@Param("urlId") Long urlId, @Param("since") LocalDateTime since);

    void deleteByUrlId(Long urlId);
}

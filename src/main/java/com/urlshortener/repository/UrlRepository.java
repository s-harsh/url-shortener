package com.urlshortener.repository;

import com.urlshortener.model.Url;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCodeAndActiveTrue(String shortCode);

    Optional<Url> findByCustomAliasAndActiveTrue(String customAlias);

    boolean existsByShortCode(String shortCode);

    boolean existsByCustomAlias(String customAlias);

    @Modifying
    @Query("UPDATE Url u SET u.clickCount = u.clickCount + 1 WHERE u.id = :id")
    int incrementClickCount(@Param("id") Long id);

    @Modifying
    @Query("UPDATE Url u SET u.active = false WHERE u.shortCode = :shortCode")
    int deactivateByShortCode(@Param("shortCode") String shortCode);
}

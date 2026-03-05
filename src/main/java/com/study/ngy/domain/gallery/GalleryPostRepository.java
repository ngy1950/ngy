package com.study.ngy.domain.gallery;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GalleryPostRepository extends JpaRepository<GalleryPost, Long> {

    List<GalleryPost> findByOrderByCreatedAtDesc();

    List<GalleryPost> findByCategoryOrderByCreatedAtDesc(String category);

    @Query("SELECT DISTINCT p FROM GalleryPost p LEFT JOIN FETCH p.images WHERE p.id = :id")
    java.util.Optional<GalleryPost> findByIdWithImages(Long id);
}

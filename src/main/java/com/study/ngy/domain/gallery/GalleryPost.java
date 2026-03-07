package com.study.ngy.domain.gallery;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gallery_post")
@Getter
@Setter
public class GalleryPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 50)
    private String category;

    private LocalDate eventDate;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    private List<GalleryImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public String getThumbnailUrl() {
        if (images.isEmpty()) return null;
        return images.get(0).getCloudinaryUrl();
    }

    public String getCategoryLabel() {
        return switch (category) {
            case "jesa"    -> "제사음식";
            case "sije"    -> "시제상";
            case "jangji"  -> "장지음식";
            case "group"   -> "단체·행사음식";
            case "dosirak" -> "도시락";
            default        -> category;
        };
    }
}

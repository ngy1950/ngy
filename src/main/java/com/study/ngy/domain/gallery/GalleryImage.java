package com.study.ngy.domain.gallery;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "gallery_image")
@Getter
@Setter
public class GalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private GalleryPost post;

    @Column(nullable = false, length = 500)
    private String cloudinaryUrl;

    @Column(name = "display_order")
    private int displayOrder;
}

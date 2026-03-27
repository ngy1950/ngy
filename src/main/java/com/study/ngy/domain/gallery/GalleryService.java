package com.study.ngy.domain.gallery;

import com.study.ngy.util.CloudinaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryService {

    private final GalleryPostRepository postRepository;
    private final GalleryImageRepository imageRepository;
    private final CloudinaryService cloudinaryService;

    public List<GalleryPost> findRecent() {
        return postRepository.findTop4ByOrderByEventDateDescCreatedAtDesc();
    }

    public List<GalleryPost> findAll() {
        return postRepository.findByOrderByEventDateDescCreatedAtDesc();
    }

    public List<GalleryPost> findByCategory(String category) {
        return postRepository.findByCategoryOrderByEventDateDescCreatedAtDesc(category);
    }

    public GalleryPost findById(Long id) {
        return postRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("게시물을 찾을 수 없습니다: " + id));
    }

    @Transactional
    public GalleryPost create(String title, String description, String category,
                              LocalDate eventDate, List<MultipartFile> files) throws IOException {
        GalleryPost post = new GalleryPost();
        post.setTitle(title);
        post.setDescription(description);
        post.setCategory(category);
        post.setEventDate(eventDate);
        postRepository.save(post);

        int order = 0;
        for (MultipartFile file : files) {
            if (file.isEmpty()) continue;
            String url = cloudinaryService.upload(file);
            GalleryImage image = new GalleryImage();
            image.setPost(post);
            image.setCloudinaryUrl(url);
            image.setDisplayOrder(order++);
            imageRepository.save(image);
            post.getImages().add(image);
        }
        return post;
    }

    @Transactional
    public void delete(Long id) {
        GalleryPost post = findById(id);
        // Cloudinary 이미지는 무료 플랜에서 API로 삭제 가능하나 여기서는 DB만 삭제
        postRepository.delete(post);
    }
}

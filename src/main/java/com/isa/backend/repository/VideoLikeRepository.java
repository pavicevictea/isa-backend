package com.isa.backend.repository;

import com.isa.backend.model.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    Optional<VideoLike> findByUserIdAndVideoId(Long userId, Long videoId);
    Long countByVideoId(Long videoId);
    boolean existsByUserIdAndVideoId(Long userId, Long videoId);
}

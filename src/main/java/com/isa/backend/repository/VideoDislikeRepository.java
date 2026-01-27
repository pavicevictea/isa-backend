package com.isa.backend.repository;

import com.isa.backend.model.VideoDislike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoDislikeRepository extends JpaRepository<VideoDislike, Long> {
    Long countByVideoId(Long videoId);
    boolean existsByUserIdAndVideoId(Long userId, Long videoId);
    Optional<VideoDislike> findByUserIdAndVideoId(Long userId, Long videoId);
    List<VideoDislike> findAllByCreatedAtAfter(LocalDateTime dateTime);
}

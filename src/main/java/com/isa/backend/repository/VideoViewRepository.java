package com.isa.backend.repository;

import com.isa.backend.model.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoViewRepository extends JpaRepository<VideoView, Long> {
    List<VideoView> findAllByViewedAtAfter(LocalDateTime time);
}

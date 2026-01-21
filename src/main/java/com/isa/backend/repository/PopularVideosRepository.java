package com.isa.backend.repository;

import com.isa.backend.model.PopularVideos;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PopularVideosRepository extends JpaRepository<PopularVideos, Long> {
    PopularVideos findTopByOrderByRunTimeDesc();
}

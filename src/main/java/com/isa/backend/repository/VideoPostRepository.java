package com.isa.backend.repository;

import com.isa.backend.model.VideoPost;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VideoPostRepository extends JpaRepository<VideoPost, Long>{

    @Transactional
    @Modifying
    @Query("UPDATE VideoPost v SET v.views = v.views + 1 WHERE v.id = :id")
    void incrementViews(@Param("id") Long id);
}

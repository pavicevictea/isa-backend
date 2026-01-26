package com.isa.backend.repository;

import com.isa.backend.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByVideoIdOrderByCreatedAtDesc(Long videoId, Pageable pageable);
    long countByUserIdAndCreatedAtAfter(Long userId, LocalDateTime timestamp);
    List<Comment> findAllByCreatedAtAfter(LocalDateTime dateTime);
}

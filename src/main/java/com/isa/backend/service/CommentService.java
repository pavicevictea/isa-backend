package com.isa.backend.service;

import com.isa.backend.dto.CommentRequestDto;
import com.isa.backend.dto.CommentResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {
    void addComment(Long videoId, CommentRequestDto dto, String username);
    Page<CommentResponseDto> getVideoComments(Long videoId, Pageable pageable);
}

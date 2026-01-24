package com.isa.backend.service.impl;

import com.isa.backend.dto.CommentRequestDto;
import com.isa.backend.dto.CommentResponseDto;
import com.isa.backend.model.Comment;
import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.CommentRepository;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class CommentServiceImpl implements CommentService {
    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Override
    @Transactional
    @CacheEvict(value = "comments", allEntries = true)
    public void addComment(Long videoId, CommentRequestDto dto, String username){
        User user = userRepository.findByUsername(username);
        if(user == null) {
            throw new RuntimeException("User not found!");
        }

        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long count = commentRepository.countByUserIdAndCreatedAtAfter(user.getId(), oneHourAgo);
        if (count >= 60) {
            throw new RuntimeException("Rate limit exceeded: You can only post 60 comments per hour.");
        }

        VideoPost video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found!"));

        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setUser(user);
        comment.setVideo(video);
        comment.setCreatedAt(LocalDateTime.now());

        commentRepository.save(comment);
    }

    @Override
    @Cacheable(value = "comments", key = "#videoId + '-' + #pageable.pageNumber")
    public Page<CommentResponseDto> getVideoComments(Long videoId, Pageable pageable){
        return commentRepository.findByVideoIdOrderByCreatedAtDesc(videoId, pageable)
                .map(comment -> {
                    CommentResponseDto res = new CommentResponseDto();
                    res.setId(comment.getId());
                    res.setText(comment.getText());
                    res.setCreatedAt(comment.getCreatedAt());
                    res.setAuthorUsername(comment.getUser().getUsername());
                    res.setFirstName(comment.getUser().getFirstName());
                    res.setLastName(comment.getUser().getLastName());
                    return res;
                });
    }
}

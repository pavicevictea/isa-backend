package com.isa.backend.unit_tests;

import com.isa.backend.dto.CommentRequestDto;
import com.isa.backend.model.Comment;
import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.CommentRepository;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.service.CommentService;
import com.isa.backend.service.impl.CommentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoCommentTests {

    @InjectMocks
    private CommentServiceImpl commentService;

    private CommentService service;

    @Mock private CommentRepository commentRepository;
    @Mock private UserRepository userRepository;
    @Mock private VideoPostRepository videoPostRepository;

    private User firstUser;
    private User secondUser;
    private VideoPost video;
    private CommentRequestDto request;

    @BeforeEach
    void setUp() {
        service = commentService;

        firstUser = new User();
        firstUser.setId(1L);
        firstUser.setUsername("tea");

        secondUser = new User();
        secondUser.setId(2L);
        secondUser.setUsername("ana");

        video = new VideoPost();
        video.setId(10L);

        request = new CommentRequestDto();
        request.setText("Great video!");
    }

    private Comment createComment(Long id, String text, User user, VideoPost video, LocalDateTime createdAt){
        Comment c = new Comment();
        c.setId(id);
        c.setText(text);
        c.setUser(user);
        c.setVideo(video);
        c.setCreatedAt(createdAt);
        return c;
    }

    @Test
    void addComment_withValidData(){
        when(userRepository.findByUsername("tea")).thenReturn(firstUser);
        when(commentRepository.countByUserIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(0L);
        when(videoPostRepository.findById(10L)).thenReturn(Optional.of(video));

        ArgumentCaptor<Comment> captor = ArgumentCaptor.forClass(Comment.class);

        service.addComment(10L, request, "tea");
        verify(commentRepository, times(1)).save(captor.capture());
        Comment saved = captor.getValue();

        assertEquals("Great video!", saved.getText());
        assertEquals(firstUser, saved.getUser());
        assertEquals(video, saved.getVideo());
        assertNotNull(saved.getCreatedAt());
    }

    @Test
    void addComment_unregisteredUser(){
        when(userRepository.findByUsername("anonymous")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.addComment(10L, request, "anonymous"));
        assertTrue(ex.getMessage().contains("User not found"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_withBlankText(){
        when(userRepository.findByUsername("tea")).thenReturn(firstUser);
        when(commentRepository.countByUserIdAndCreatedAtAfter(eq(1L), any())).thenReturn(0L);
        when(videoPostRepository.findById(10L)).thenReturn(Optional.of(video));

        CommentRequestDto blankRequest = new CommentRequestDto();
        blankRequest.setText("   ");

        assertThrows(RuntimeException.class, () -> service.addComment(10L, blankRequest, "tea"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addComment_whenRateLimitReached(){
        when(userRepository.findByUsername("tea")).thenReturn(firstUser);
        when(commentRepository.countByUserIdAndCreatedAtAfter(eq(1L), any(LocalDateTime.class))).thenReturn(60L);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.addComment(10L, request, "tea"));
        assertTrue(ex.getMessage().contains("Rate limit exceeded"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    void addCommentWithDifferentUser_whenRateLimitReached(){
        when(userRepository.findByUsername("ana")).thenReturn(secondUser);
        when(commentRepository.countByUserIdAndCreatedAtAfter(eq(2L), any(LocalDateTime.class))).thenReturn(0L);
        when(videoPostRepository.findById(10L)).thenReturn(Optional.of(video));

        CommentRequestDto commentRequest = new CommentRequestDto();
        commentRequest.setText("Comment from a different user.");

        assertDoesNotThrow(() -> service.addComment(10L, commentRequest, "ana"));
        verify(commentRepository, times(1)).save(any(Comment.class));
    }
}


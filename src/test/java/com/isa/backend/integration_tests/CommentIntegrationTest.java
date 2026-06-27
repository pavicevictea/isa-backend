package com.isa.backend.integration_tests;

import com.isa.backend.dto.CommentRequestDto;
import com.isa.backend.dto.CommentResponseDto;
import com.isa.backend.integration_tests.config.IntegrationTestConfig;
import com.isa.backend.model.Comment;
import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.CommentRepository;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.repository.VideoViewRepository;
import com.isa.backend.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
class CommentIntegrationTest {

    @Autowired private CommentService commentService;
    @Autowired private CommentRepository commentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private VideoPostRepository videoPostRepository;
    @Autowired private VideoViewRepository videoViewRepository;

    private User firstUser;
    private User secondUser;
    private VideoPost testVideo;
    private VideoPost secondVideo;

    @BeforeEach
    void setUp() {
        videoViewRepository.deleteAll();
        commentRepository.deleteAll();
        videoPostRepository.deleteAll();
        userRepository.deleteAll();

        firstUser = new User();
        firstUser.setUsername("firstUser");
        firstUser.setPassword("$2a$10$hashedpassword");
        firstUser.setFirstName("First");
        firstUser.setLastName("User");
        firstUser.setEmail("first@test.com");
        firstUser.setEnabled(true);
        firstUser = userRepository.save(firstUser);

        secondUser = new User();
        secondUser.setUsername("seconduser");
        secondUser.setPassword("$2a$10$hashedpassword");
        secondUser.setFirstName("Second");
        secondUser.setLastName("User");
        secondUser.setEmail("second@test.com");
        secondUser.setEnabled(true);
        secondUser = userRepository.save(secondUser);

        testVideo = new VideoPost();
        testVideo.setTitle("Test Video");
        testVideo.setVideoPath("./test-uploads/videos/test.mp4");
        testVideo.setThumbnailPath("./test-uploads/thumbnails/test.jpg");
        testVideo.setUser(firstUser);
        testVideo.setViews(0L);
        testVideo = videoPostRepository.save(testVideo);

        secondVideo = new VideoPost();
        secondVideo.setTitle("Second Video");
        secondVideo.setVideoPath("./test-uploads/videos/second.mp4");
        secondVideo.setThumbnailPath("./test-uploads/thumbnails/second.jpg");
        secondVideo.setUser(firstUser);
        secondVideo.setViews(0L);
        secondVideo = videoPostRepository.save(secondVideo);
    }

    @Test
    void addComment_validComment() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Great video!");

        assertDoesNotThrow(() -> commentService.addComment(testVideo.getId(), dto, firstUser.getUsername()));

        List<Comment> comments = commentRepository.findAll();
        assertEquals(1, comments.size());
        assertEquals("Great video!", comments.get(0).getText());
    }

    @Test
    void addComment_unregisteredUser() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Comment text");

        assertThrows(RuntimeException.class, () -> commentService.addComment(testVideo.getId(), dto, null));

        assertEquals(0, commentRepository.count());
    }

    @Test
    @Transactional
    void addComment_hasAllAdditionalInfo() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("Comment text");

        LocalDateTime beforeSave = LocalDateTime.now().minusSeconds(1);
        commentService.addComment(testVideo.getId(), dto, firstUser.getUsername());
        LocalDateTime afterSave = LocalDateTime.now().plusSeconds(1);

        Pageable pageable = PageRequest.of(0, 10);
        Page<CommentResponseDto> page = commentService.getVideoComments(testVideo.getId(), pageable);

        CommentResponseDto saved = page.getContent().get(0);

        assertNotNull(saved.getCreatedAt());
        assertTrue(saved.getCreatedAt().isAfter(beforeSave) && saved.getCreatedAt().isBefore(afterSave));
        assertEquals(firstUser.getUsername(), saved.getAuthorUsername());
        assertEquals(firstUser.getFirstName(), saved.getFirstName());
        assertEquals(firstUser.getLastName(), saved.getLastName());
    }

    @Test
    void addComment_withBlankText() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("");

        try {
            commentService.addComment(testVideo.getId(), dto, firstUser.getUsername());
            List<Comment> comments = commentRepository.findAll();
            assertTrue(comments.isEmpty() || comments.stream().noneMatch(c -> c.getText().isEmpty()));
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    @Test
    void addComment_withWhiteSpaceOnly() {
        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("     ");

        try {
            commentService.addComment(testVideo.getId(), dto, firstUser.getUsername());
            List<Comment> comments = commentRepository.findAll();
            assertTrue(comments.isEmpty() || comments.stream().noneMatch(c -> c.getText().isBlank()));
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    @Test
    @Transactional
    void getVideoComments_pagination() {
        for (int i = 1; i <= 15; i++) {
            Comment comment = new Comment();
            comment.setText("Comment number " + i);
            comment.setUser(firstUser);
            comment.setVideo(testVideo);
            comment.setCreatedAt(LocalDateTime.now().minusMinutes(15 - i));
            commentRepository.save(comment);
        }
        Pageable firstPage = PageRequest.of(0, 10);
        Page<CommentResponseDto> page0 = commentService.getVideoComments(testVideo.getId(), firstPage);
        Pageable secondPage = PageRequest.of(1, 10);
        Page<CommentResponseDto> page1 = commentService.getVideoComments(testVideo.getId(), secondPage);

        assertEquals(10, page0.getContent().size());
        assertEquals(5, page1.getContent().size());
        assertEquals(15, page0.getTotalElements());
        assertEquals(2, page0.getTotalPages());
        assertFalse(page1.hasNext());
    }

    @Test
    void addComment_whenRateLimitReached() {
        for (int i = 1; i <= 60; i++) {
            Comment comment = new Comment();
            comment.setText("Comment number " + i);
            comment.setUser(firstUser);
            comment.setVideo(testVideo);
            comment.setCreatedAt(LocalDateTime.now().minusMinutes(30));
            commentRepository.save(comment);
        }

        CommentRequestDto dto = new CommentRequestDto();
        dto.setText("61st comment - should be rejected");

        RuntimeException exception = assertThrows(RuntimeException.class, () -> commentService.addComment(testVideo.getId(), dto, firstUser.getUsername()));

        assertTrue(exception.getMessage().contains("Rate limit") || exception.getMessage().contains("60"));
        assertEquals(60, commentRepository.count());
    }

    @Test
    void addCommentWithDifferentUser_whenRateLimitReached() {
        for (int i = 1; i <= 60; i++) {
            Comment comment = new Comment();
            comment.setText("Comment number " + i);
            comment.setUser(firstUser);
            comment.setVideo(testVideo);
            comment.setCreatedAt(LocalDateTime.now().minusMinutes(30));
            commentRepository.save(comment);
        }
        CommentRequestDto blockedDto = new CommentRequestDto();
        blockedDto.setText("Should be blocked");
        assertThrows(RuntimeException.class, () -> commentService.addComment(testVideo.getId(), blockedDto, firstUser.getUsername()));

        CommentRequestDto allowedDto = new CommentRequestDto();
        allowedDto.setText("Comment from the second user");

        assertDoesNotThrow(() -> commentService.addComment(testVideo.getId(), allowedDto, secondUser.getUsername()));
        List<Comment> allComments = commentRepository.findAll();
        assertTrue(allComments.stream().anyMatch(c -> c.getUser().getUsername().equals(secondUser.getUsername()) && c.getText().equals("Comment from the second user")));
    }
}

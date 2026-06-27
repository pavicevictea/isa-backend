package com.isa.backend.integration_tests;

import com.isa.backend.integration_tests.config.IntegrationTestConfig;
import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.repository.VideoViewRepository;
import com.isa.backend.service.VideoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
class VideoPostIntegrationTests {

    @Autowired private VideoService videoService;
    @Autowired private VideoPostRepository videoPostRepository;
    @Autowired private VideoViewRepository videoViewRepository;
    @Autowired private UserRepository userRepository;

    private User testUser;
    private VideoPost testVideo;

    @BeforeEach
    void setUp() {
        videoViewRepository.deleteAll();
        videoPostRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$hashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@test.com");
        testUser.setEnabled(true);
        testUser = userRepository.save(testUser);

        testVideo = new VideoPost();
        testVideo.setTitle("Test Video");
        testVideo.setDescription("Test Description");
        testVideo.setVideoPath("./test-uploads/videos/test.mp4");
        testVideo.setThumbnailPath("./test-uploads/thumbnails/test.jpg");
        testVideo.setUser(testUser);
        testVideo.setViews(0L);
        testVideo = videoPostRepository.save(testVideo);
    }

    @Test
    void getAllVideos_shouldBeVisibleToOtherUsers() {
        User otherUser = new User();
        otherUser.setUsername("seconduser");
        otherUser.setPassword("$2a$10$hashedpassword");
        otherUser.setFirstName("Second");
        otherUser.setLastName("User");
        otherUser.setEmail("second@test.com");
        otherUser.setEnabled(true);
        userRepository.save(otherUser);

        List<VideoPost> videosSeenByOther = videoService.getAllVideos();

        assertFalse(videosSeenByOther.isEmpty());
        assertEquals(testVideo.getId(), videosSeenByOther.get(0).getId());
        assertEquals("Test Video", videosSeenByOther.get(0).getTitle());
    }
}
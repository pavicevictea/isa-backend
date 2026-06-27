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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(IntegrationTestConfig.class)
class ViewCounterIntegrationTest {

    @Autowired private VideoService videoService;
    @Autowired private VideoPostRepository videoPostRepository;
    @Autowired private VideoViewRepository videoViewRepository;
    @Autowired private UserRepository userRepository;

    private VideoPost testVideo;
    private VideoPost secondVideo;

    @BeforeEach
    void setUp() {
        videoViewRepository.deleteAll();
        videoPostRepository.deleteAll();
        userRepository.deleteAll();

        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("$2a$10$hashedpassword");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setEmail("test@test.com");
        testUser.setEnabled(true);
        userRepository.save(testUser);

        testVideo = new VideoPost();
        testVideo.setTitle("Test Video");
        testVideo.setVideoPath("./test-uploads/videos/test.mp4");
        testVideo.setThumbnailPath("./test-uploads/thumbnails/test.jpg");
        testVideo.setUser(testUser);
        testVideo.setViews(0L);
        testVideo = videoPostRepository.save(testVideo);

        secondVideo = new VideoPost();
        secondVideo.setTitle("Second Video");
        secondVideo.setVideoPath("./test-uploads/videos/second.mp4");
        secondVideo.setThumbnailPath("./test-uploads/thumbnails/second.jpg");
        secondVideo.setUser(testUser);
        secondVideo.setViews(0L);
        secondVideo = videoPostRepository.save(secondVideo);
    }

    @Test
    void recordView_singleVisit() {
        Long viewsBefore = videoPostRepository.findById(testVideo.getId()).orElseThrow().getViews();

        videoService.recordView(testVideo.getId());

        Long viewsAfter = videoPostRepository.findById(testVideo.getId()).orElseThrow().getViews();

        assertEquals(viewsBefore + 1, viewsAfter);
    }

    @Test
    void recordView_viewCountIsVisibleInVideoDetails() {
        videoService.recordView(testVideo.getId());
        videoService.recordView(testVideo.getId());

        var details = videoService.getVideoDetails(testVideo.getId(), null);

        assertNotNull(details);
        assertEquals(2L, details.getViews());
    }

    @Test
    void recordView_multipleConsecutiveVisits() {
        int numberOfVisits = 5;

        for (int i = 0; i < numberOfVisits; i++) {
            videoService.recordView(testVideo.getId());
        }

        Long viewsAfter = videoPostRepository.findById(testVideo.getId()).orElseThrow().getViews();

        assertEquals(numberOfVisits, viewsAfter);
    }

    @Test
    void recordView_differentVideosHaveDifferentCounters() {
        videoService.recordView(testVideo.getId());
        videoService.recordView(testVideo.getId());
        videoService.recordView(secondVideo.getId());

        Long firstVideoViews = videoPostRepository.findById(testVideo.getId()).orElseThrow().getViews();
        Long secondVideoViews = videoPostRepository.findById(secondVideo.getId()).orElseThrow().getViews();

        assertEquals(2L, firstVideoViews);
        assertEquals(1L, secondVideoViews);
    }

    @Test
    void recordView_concurrentVisits() throws InterruptedException {
        int numberOfConcurrentUsers = 20;
        CountDownLatch latch = new CountDownLatch(numberOfConcurrentUsers);
        ExecutorService executor = Executors.newFixedThreadPool(numberOfConcurrentUsers);
        List<Exception> errors = new ArrayList<>();

        for (int i = 0; i < numberOfConcurrentUsers; i++) {
            executor.submit(() -> {
                try {
                    videoService.recordView(testVideo.getId());
                } catch (Exception e) {
                    synchronized (errors) {
                        errors.add(e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        boolean finished = latch.await(15, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished);
        assertTrue(errors.isEmpty());
        Long finalViews = videoPostRepository.findById(testVideo.getId()).orElseThrow().getViews();
        assertEquals(numberOfConcurrentUsers, finalViews);
    }
}

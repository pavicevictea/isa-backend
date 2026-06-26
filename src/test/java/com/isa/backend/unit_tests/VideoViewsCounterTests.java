package com.isa.backend.unit_tests;

import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.model.VideoView;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.repository.VideoViewRepository;
import com.isa.backend.service.VideoService;
import com.isa.backend.service.impl.VideoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.Optional;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VideoViewsCounterTests {

    @InjectMocks
    private VideoServiceImpl videoService;

    private VideoService service;

    @Mock private VideoPostRepository videoPostRepository;
    @Mock private VideoViewRepository videoViewRepository;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;

    @BeforeEach
    void setUp(){
        service = videoService;
    }

    private VideoPost createTestVideo(Long id) {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        VideoPost video = new VideoPost();
        video.setId(id);
        video.setUser(user);
        return video;
    }

    @Test
    void recordView_singleVisit() {
        VideoPost video = createTestVideo(42L);

        doNothing().when(videoPostRepository).incrementViews(42L);
        when(videoPostRepository.findById(42L)).thenReturn(Optional.of(video));
        when(videoViewRepository.save(any(VideoView.class))).thenReturn(new VideoView());
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));

        service.recordView(42L);

        verify(videoPostRepository, times(1)).incrementViews(42L);
        verify(videoViewRepository, times(1)).save(any(VideoView.class));
    }

    @Test
    void recordView_multipleConsecutiveVisits() {
        VideoPost video = createTestVideo(42L);

        doNothing().when(videoPostRepository).incrementViews(42L);
        when(videoPostRepository.findById(42L)).thenReturn(Optional.of(video));
        when(videoViewRepository.save(any(VideoView.class))).thenReturn(new VideoView());
        doNothing().when(simpMessagingTemplate).convertAndSend(anyString(), Optional.ofNullable(any()));

        service.recordView(42L);
        service.recordView(42L);
        service.recordView(42L);

        verify(videoPostRepository, times(3)).incrementViews(42L);
        verify(videoViewRepository, times(3)).save(any(VideoView.class));
    }
}

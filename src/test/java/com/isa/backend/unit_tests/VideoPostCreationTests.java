package com.isa.backend.unit_tests;

import com.isa.backend.dto.LocationDto;
import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.Location;
import com.isa.backend.model.User;
import com.isa.backend.model.VideoPost;
import com.isa.backend.repository.LocationRepository;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.service.MessageBenchmarkService;
import com.isa.backend.service.VideoService;
import com.isa.backend.service.impl.VideoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VideoPostCreationTests {

    @InjectMocks
    private VideoServiceImpl videoService;

    private VideoService service;

    @Mock private VideoPostRepository videoPostRepository;
    @Mock private UserRepository userRepository;
    @Mock private LocationRepository locationRepository;
    @Mock private SimpMessagingTemplate simpMessagingTemplate;
    @Mock private MessageBenchmarkService benchmarkService;

    @Mock private MultipartFile videoFile;
    @Mock private MultipartFile thumbnailFile;

    private User mockUser;
    private VideoPostUploadDto mockDto;

    @BeforeEach
    void setUp(){
        service = videoService;

        ReflectionTestUtils.setField(videoService, "videoDir", System.getProperty("java.io.tmpdir") + "/videos");
        ReflectionTestUtils.setField(videoService, "thumbnailDir", System.getProperty("java.io.tmpdir") + "/thumbnails");

        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");

        LocationDto locationDto = new LocationDto();
        locationDto.setLatitude(44.7866);
        locationDto.setLongitude(20.4489);
        locationDto.setDisplayName("Belgrade");
        locationDto.setCity("Belgrade");
        locationDto.setCountry("Serbia");

        mockDto = new VideoPostUploadDto();
        mockDto.setTitle("Test video");
        mockDto.setDescription("Test video description");
        mockDto.setTags(List.of("test1", "test2"));
        mockDto.setLocation(locationDto);
    }

    @Test
    void createVideoPost_withValidData() throws IOException{
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenReturn(new Location());
        when(videoFile.getOriginalFilename()).thenReturn("video.mp4");
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(videoFile.getSize()).thenReturn(1024L);
        when(thumbnailFile.getOriginalFilename()).thenReturn("thumb.jpg");
        when(thumbnailFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{4, 5, 6}));
        doNothing().when(benchmarkService).sendUploadEvents(any());

        VideoPost savedPost = new VideoPost();
        savedPost.setId(1L);
        savedPost.setTitle("Test video");
        savedPost.setUser(mockUser);
        savedPost.setVideoPath(System.getProperty("java.io.tmpdir") + "/videos/video.mp4");
        when(videoPostRepository.save(any(VideoPost.class))).thenReturn(savedPost);

        VideoPost result = service.createVideoPost(mockDto, videoFile, thumbnailFile, "testuser");

        assertNotNull(result);
        assertEquals("Test video", result.getTitle());
        verify(videoPostRepository, times(1)).save(any(VideoPost.class));
    }

    @Test
    void createVideoPost_withBlankTitle() throws IOException {
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenReturn(new Location());
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(videoFile.getOriginalFilename()).thenReturn("video.mp4");
        when(thumbnailFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{4, 5, 6}));
        when(thumbnailFile.getOriginalFilename()).thenReturn("thumb.jpg");
        when(videoPostRepository.save(any(VideoPost.class))).thenReturn(new VideoPost());
        doNothing().when(benchmarkService).sendUploadEvents(any());
        mockDto.setTitle("");

        assertDoesNotThrow(() -> service.createVideoPost(mockDto, videoFile, thumbnailFile, "testuser"));
        verify(videoPostRepository, never()).save(any());
    }

    @Test
    void createVideoPost_withFileLargerThan200MB() throws IOException {
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenReturn(new Location());
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(videoFile.getOriginalFilename()).thenReturn("video.mp4");
        when(videoFile.getSize()).thenReturn(201L * 1024 * 1024);
        when(thumbnailFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{4, 5, 6}));
        when(thumbnailFile.getOriginalFilename()).thenReturn("thumb.jpg");
        when(videoPostRepository.save(any(VideoPost.class))).thenReturn(new VideoPost());
        doNothing().when(benchmarkService).sendUploadEvents(any());

        assertDoesNotThrow(() -> service.createVideoPost(mockDto, videoFile, thumbnailFile, "testuser"));
        verify(videoPostRepository, never()).save(any());
    }

    @Test
    void createVideoPost_withNonMp4File() throws IOException {
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(locationRepository.save(any(Location.class))).thenReturn(new Location());
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1, 2, 3}));
        when(videoFile.getOriginalFilename()).thenReturn("video.mov");
        when(videoFile.getSize()).thenReturn(1024L);
        when(thumbnailFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{4, 5, 6}));
        when(thumbnailFile.getOriginalFilename()).thenReturn("thumb.jpg");
        when(videoPostRepository.save(any(VideoPost.class))).thenReturn(new VideoPost());
        doNothing().when(benchmarkService).sendUploadEvents(any());

        assertDoesNotThrow(() -> service.createVideoPost(mockDto, videoFile, thumbnailFile, "testuser"));
        verify(videoPostRepository, never()).save(any());
    }

    @Test
    void createVideoPost_whenRepositoryFails() throws IOException{
        when(userRepository.findByUsername("testuser")).thenReturn(mockUser);
        when(locationRepository.findByLatitudeAndLongitude(anyDouble(), anyDouble())).thenReturn(Optional.empty());
        when(locationRepository.save(any())).thenReturn(new Location());
        when(videoFile.getOriginalFilename()).thenReturn("video.mp4");
        when(videoFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{1}));
        when(videoFile.getSize()).thenReturn(1024L);
        when(thumbnailFile.getOriginalFilename()).thenReturn("thumb.jpg");
        when(thumbnailFile.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[]{2}));
        when(videoPostRepository.save(any(VideoPost.class))).thenThrow(new RuntimeException("Database error - rollback expected"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.createVideoPost(mockDto, videoFile, thumbnailFile, "testuser"));

        assertTrue(ex.getMessage().contains("Database error"));
        verify(videoPostRepository, times(1)).save(any());
    }

    @Test
    void getThumbnail_hasCacheableAnnotation() throws Exception {
        var method = VideoServiceImpl.class.getMethod("getThumbnail", Long.class);
        var cacheable = method.getAnnotation(Cacheable.class);

        assertNotNull(cacheable, "@Cacheable annotation must be present on getThumbnail()");
        assertEquals("thumbnails", cacheable.value()[0]);
        assertEquals("#videoId", cacheable.key());
    }

}

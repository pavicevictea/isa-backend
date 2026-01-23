package com.isa.backend.service.impl;

import com.isa.backend.dto.VideoPostResponseDto;
import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.VideoLike;
import com.isa.backend.model.VideoPost;
import com.isa.backend.model.User;
import com.isa.backend.model.VideoView;
import com.isa.backend.repository.VideoLikeRepository;
import com.isa.backend.repository.VideoPostRepository;
import com.isa.backend.repository.UserRepository;
import com.isa.backend.repository.VideoViewRepository;
import com.isa.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class VideoServiceImpl implements VideoService{

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Value("${storage.video-path}")
    private String videoDir;

    @Value("${storage.thumbnail-path}")
    private String thumbnailDir;

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    //@Transactional(rollbackFor = Exception.class, timeout = 1)
    public VideoPost createVideoPost(VideoPostUploadDto dto, MultipartFile videoFile, MultipartFile thumbnailFile, String username) throws IOException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new RuntimeException("User with username " + username + "not found!");
        }

        Files.createDirectories(Paths.get(videoDir));
        Files.createDirectories(Paths.get(thumbnailDir));

        String fileName = UUID.randomUUID().toString() + "_" + videoFile.getOriginalFilename();
        Path videoPath = Paths.get(videoDir, fileName);
        String thumbnailName = UUID.randomUUID().toString() + "_" + thumbnailFile.getOriginalFilename();
        Path thumbnailPath = Paths.get(thumbnailDir, thumbnailName);

        Files.copy(videoFile.getInputStream(), videoPath);
        Files.copy(thumbnailFile.getInputStream(), thumbnailPath);

        //Simulacija rollback operacije
        //try { Thread.sleep(3000); } catch (InterruptedException e) {}

        try {
            VideoPost post = new VideoPost();
            post.setTitle(dto.getTitle());
            post.setDescription(dto.getDescription());
            post.setTags(dto.getTags());
            post.setLocation(dto.getLocation());
            post.setVideoPath(videoPath.toString());
            post.setThumbnailPath(thumbnailPath.toString());
            post.setUser(user);

            return videoPostRepository.save(post);
        } catch(Exception e) {
            Files.deleteIfExists(videoPath);
            Files.deleteIfExists(thumbnailPath);
            throw e;
        }
    }

    @Override
    @Cacheable(value = "thumbnails", key = "#videoId")
    public byte[] getThumbnail(Long videoId) throws IOException {
        VideoPost post = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video post not found with id: " + videoId));

        Path path = Paths.get(post.getThumbnailPath());
        return Files.readAllBytes(path);
    }

    @Override
    public List<VideoPost> getAllVideos() {
        return videoPostRepository.findAll();
    }

    @Override
    public VideoPost getVideoById(Long id) {
        return videoPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
    }

    @Override
    @Transactional
    public void recordView(Long id) {
        videoPostRepository.incrementViews(id);
        VideoPost video = videoPostRepository.findById(id).orElseThrow();

        VideoView view = new VideoView();
        view.setVideo(video);
        view.setViewedAt(LocalDateTime.now());
        videoViewRepository.save(view);

        this.simpMessagingTemplate.convertAndSend("/socket-publisher/video-views", video);
    }

    @Override
    public VideoPost findOnlyById(Long id) {
        return videoPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found with id: " + id));
    }

    @Override
    public VideoPostResponseDto getVideoDetails(Long id, String currentUsername) {
        VideoPost video = videoPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        VideoPostResponseDto dto = new VideoPostResponseDto();
        dto.setId(video.getId());
        dto.setTitle(video.getTitle());
        dto.setDescription(video.getDescription());
        dto.setTags(video.getTags());
        dto.setVideoPath(video.getVideoPath());
        dto.setCreatedAt(video.getCreatedAt());
        dto.setLocation(video.getLocation());
        dto.setViews(video.getViews());
        dto.setAuthorUsername(video.getUser().getUsername());
        dto.setFirstName(video.getUser().getFirstName());
        dto.setLastName(video.getUser().getLastName());

        dto.setLikesCount(videoLikeRepository.countByVideoId(id));
        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername);
            if (currentUser != null) {
                dto.setLikedByCurrentUser(videoLikeRepository.existsByUserIdAndVideoId(currentUser.getId(), id));
            }
        } else {
            dto.setLikedByCurrentUser(false);
        }
        return dto;
    }

    @Override
    @Transactional
    public void toggleLike(Long videoId, String username) {
        User user = userRepository.findByUsername(username);
        VideoPost video = videoPostRepository.findById(videoId).orElseThrow();

        videoLikeRepository.findByUserIdAndVideoId(user.getId(), videoId)
                .ifPresentOrElse(
                        videoLikeRepository::delete,
                        () -> videoLikeRepository.save(new VideoLike(user, video))
                );

        this.simpMessagingTemplate.convertAndSend("/socket-publisher/video-likes/" + videoId, videoLikeRepository.countByVideoId(videoId));
    }
}

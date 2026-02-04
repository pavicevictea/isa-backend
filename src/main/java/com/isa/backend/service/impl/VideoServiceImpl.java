package com.isa.backend.service.impl;

import com.isa.backend.dto.LocationDto;
import com.isa.backend.dto.StreamingStatusDto;
import com.isa.backend.dto.VideoPostResponseDto;
import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.*;
import com.isa.backend.repository.*;
import com.isa.backend.service.PopularVideosService;
import com.isa.backend.service.VideoService;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mp4parser.IsoFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
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

    @Autowired
    private VideoDislikeRepository videoDislikeRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private PopularVideosService popularVideosService;

    @Value("${storage.video-path}")
    private String videoDir;

    @Value("${storage.thumbnail-path}")
    private String thumbnailDir;

    @Value("${app.video.default-radius}")
    private Double defaultRadius;

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30)
    //@Transactional(rollbackFor = Exception.class, timeout = 1)
    public VideoPost createVideoPost(VideoPostUploadDto dto, MultipartFile videoFile, MultipartFile thumbnailFile, String username) throws IOException {
        User user = userRepository.findByUsername(username);
        if(user == null){
            throw new RuntimeException("User with username " + username + "not found!");
        }

        LocationDto locationDto = dto.getLocation();
        if (locationDto == null) {
            throw new RuntimeException("Location data is missing!");
        }

        Location location = locationRepository.findByLatitudeAndLongitude(locationDto.getLatitude(), locationDto.getLongitude())
                .orElseGet(() -> {
                    Location newLoc = new Location();
                    newLoc.setDisplayName(locationDto.getDisplayName());
                    newLoc.setLatitude(locationDto.getLatitude());
                    newLoc.setLongitude(locationDto.getLongitude());
                    newLoc.setCity(locationDto.getCity());
                    newLoc.setCountry(locationDto.getCountry());
                    GeometryFactory gf = new GeometryFactory(new PrecisionModel(), 4326);
                    Point point = gf.createPoint(new Coordinate(locationDto.getLongitude(), locationDto.getLatitude()));
                    newLoc.setCoordinates(point);
                    return locationRepository.save(newLoc);
                });

        Files.createDirectories(Paths.get(videoDir));
        Files.createDirectories(Paths.get(thumbnailDir));

        String fileName = UUID.randomUUID().toString() + "_" + videoFile.getOriginalFilename();
        Path videoPath = Paths.get(videoDir, fileName);
        String thumbnailName = UUID.randomUUID().toString() + "_" + thumbnailFile.getOriginalFilename();
        Path thumbnailPath = Paths.get(thumbnailDir, thumbnailName);

        Files.copy(videoFile.getInputStream(), videoPath);
        Files.copy(thumbnailFile.getInputStream(), thumbnailPath);

        long durationInSeconds = 0;
        try (IsoFile isoFile = new IsoFile(videoPath.toString())) {
            long durationUnits = isoFile.getMovieBox().getMovieHeaderBox().getDuration();
            long timescale = isoFile.getMovieBox().getMovieHeaderBox().getTimescale();
            durationInSeconds = durationUnits / timescale;
        } catch (Exception e) {
            durationInSeconds = 0;
        }

        //Simulacija rollback operacije
        //try { Thread.sleep(3000); } catch (InterruptedException e) {}

        try {
            VideoPost post = new VideoPost();
            post.setTitle(dto.getTitle());
            post.setDescription(dto.getDescription());
            post.setTags(dto.getTags());
            post.setLocation(location);
            post.setVideoPath(videoPath.toString());
            post.setThumbnailPath(thumbnailPath.toString());
            post.setUser(user);
            post.setScheduledTime(dto.getScheduledTime());
            post.setDurationSeconds(durationInSeconds);

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
        dto.setScheduledTime(video.getScheduledTime());
        dto.setDurationSeconds(video.getDurationSeconds());

        if (video.getLocation() != null) {
            LocationDto locDto = new LocationDto();
            locDto.setDisplayName(video.getLocation().getDisplayName());
            locDto.setLatitude(video.getLocation().getLatitude());
            locDto.setLongitude(video.getLocation().getLongitude());
            locDto.setCity(video.getLocation().getCity());
            locDto.setCountry(video.getLocation().getCountry());
            dto.setLocation(locDto);
        }

        dto.setViews(video.getViews());
        dto.setAuthorUsername(video.getUser().getUsername());
        dto.setFirstName(video.getUser().getFirstName());
        dto.setLastName(video.getUser().getLastName());

        dto.setLikesCount(videoLikeRepository.countByVideoId(id));
        dto.setDislikesCount(videoDislikeRepository.countByVideoId(id));

        if (currentUsername != null) {
            User currentUser = userRepository.findByUsername(currentUsername);
            if (currentUser != null) {
                dto.setLikedByCurrentUser(videoLikeRepository.existsByUserIdAndVideoId(currentUser.getId(), id));
                dto.setDislikedByCurrentUser(videoDislikeRepository.existsByUserIdAndVideoId(currentUser.getId(), id));
            }
        } else {
            dto.setLikedByCurrentUser(false);
            dto.setDislikedByCurrentUser(false);
        }
        return dto;
    }

    @Override
    @Transactional
    public void toggleLike(Long videoId, String username) {
        User user = userRepository.findByUsername(username);
        VideoPost video = videoPostRepository.findById(videoId).orElseThrow();

        videoDislikeRepository.findByUserIdAndVideoId(user.getId(), videoId)
                .ifPresent(videoDislikeRepository::delete);

        videoLikeRepository.findByUserIdAndVideoId(user.getId(), videoId)
                .ifPresentOrElse(
                        videoLikeRepository::delete,
                        () -> videoLikeRepository.save(new VideoLike(user, video))
                );

        this.simpMessagingTemplate.convertAndSend("/socket-publisher/video-likes/" + videoId, videoLikeRepository.countByVideoId(videoId));
        this.simpMessagingTemplate.convertAndSend("/socket-publisher/video-dislikes/" + videoId, videoDislikeRepository.countByVideoId(videoId));
    }

    @Override
    @Transactional
    public void toggleDislike(Long videoId, String username) {
        User user = userRepository.findByUsername(username);
        VideoPost video = videoPostRepository.findById(videoId).orElseThrow();

        videoLikeRepository.findByUserIdAndVideoId(user.getId(), videoId)
                .ifPresent(videoLikeRepository::delete);

        videoDislikeRepository.findByUserIdAndVideoId(user.getId(), videoId)
                .ifPresentOrElse(
                        videoDislikeRepository::delete,
                        () -> videoDislikeRepository.save(new VideoDislike(user, video))
                );

        this.simpMessagingTemplate.convertAndSend("/socket-publisher/video-likes/" + videoId, videoLikeRepository.countByVideoId(videoId));
        this.simpMessagingTemplate.convertAndSend("/socket-publisher/video-dislikes/" + videoId, videoDislikeRepository.countByVideoId(videoId));
    }

    @Override
    public ResourceRegion getVideoStream(Long id, HttpHeaders headers) throws IOException {
        VideoPost video = videoPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        UrlResource resource = new UrlResource(Paths.get(video.getVideoPath()).toUri());
        long fileSize = resource.contentLength();
        if (video.getScheduledTime() != null && LocalDateTime.now().isBefore(video.getScheduledTime())) {
            return new ResourceRegion(resource, 0, 0);
        }

        long start = getStartFromHeaders(headers, fileSize);
        long chunkSize = 1024 * 1024;
        long rangeLength = Math.min(chunkSize, fileSize - start);
        return new ResourceRegion(resource, start, rangeLength);
    }

    private long getStartFromHeaders(HttpHeaders headers, long fileSize) {
        if (headers.getRange().isEmpty()) {
            return 0;
        }
        return headers.getRange().get(0).getRangeStart(fileSize);
    }

    @Override
    @Cacheable(value = "localTrending", key = "T(java.lang.Math).round(#lat * 100) + '_' + T(java.lang.Math).round(#lon * 100)")
    public List<VideoPost> resolveUserLocation(Double lat, Double lon, String ip) {
        if (lat == null || lon == null) {
            try {
                RestTemplate restTemplate = new RestTemplate();
                String targetIp = (ip == null || ip.contains("0:0:0:0") || ip.equals("127.0.0.1")) ? "" : ip;
                String url = "http://ip-api.com/json/" + targetIp;
                Map<String, Object> response = restTemplate.getForObject(url, Map.class);

                if (response != null && "success".equals(response.get("status"))) {
                    lat = (Double) response.get("lat");
                    lon = (Double) response.get("lon");
                    String city = (String) response.get("city");
                    System.out.println("Approximated location for IP " + ip + ": " + city + " (" + lat + ", " + lon + ")");
                } else {
                    lat = 44.7866;
                    lon = 20.4489;
                }
            } catch (Exception e) {
                lat = 44.7866;
                lon = 20.4489;
            }
        }
        return popularVideosService.getTrendingNearUser(lat, lon, defaultRadius);
    }

    @Override
    public StreamingStatusDto getStreamingStatus(Long id) {
        VideoPost video = videoPostRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(("Video not found")));

        LocalDateTime time = LocalDateTime.now();

        if (video.getScheduledTime() == null) {
            return new StreamingStatusDto(true, 0, "Normal playback");
        }

        long offset = java.time.Duration.between(video.getScheduledTime(), time).getSeconds();
        if (offset < 0) {
            return new StreamingStatusDto(false, offset, "Video starts in " + Math.abs(offset) + " seconds");
        }

        if (video.getDurationSeconds() != null && offset > video.getDurationSeconds()) {
            return new StreamingStatusDto(true, 0, "Premiere finished");
        }

        return new StreamingStatusDto(true, offset, "Live premier");
    }
}

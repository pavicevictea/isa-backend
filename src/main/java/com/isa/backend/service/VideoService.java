package com.isa.backend.service;

import com.isa.backend.dto.StreamingStatusDto;
import com.isa.backend.dto.VideoPostResponseDto;
import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.VideoPost;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface VideoService {
    VideoPost createVideoPost(VideoPostUploadDto dto, MultipartFile videoFile, MultipartFile thumbnailFile, String username) throws IOException;
    byte[] getThumbnail(Long videoId) throws IOException;
    List<VideoPost> getAllVideos();
    VideoPost getVideoById(Long id);
    VideoPost findOnlyById(Long id);
    void recordView(Long id);
    VideoPostResponseDto getVideoDetails(Long id, String currentUsername);
    void toggleLike(Long videoId, String username);
    void toggleDislike(Long videoId, String username);
    ResourceRegion getVideoStream(Long id, HttpHeaders headers) throws IOException;
    List<VideoPost> resolveUserLocation(Double lat, Double lon, String ip);
    StreamingStatusDto getStreamingStatus(Long id);
}

package com.isa.backend.service;

import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.VideoPost;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

public interface VideoService {
    VideoPost createVideoPost(VideoPostUploadDto dto, MultipartFile videoFile, MultipartFile thumbnailFile, String username) throws IOException;
    byte[] getThumbnail(Long videoId) throws IOException;
    List<VideoPost> getAllVideos();
    VideoPost getVideoById(Long id);
    VideoPost findOnlyById(Long id);
}

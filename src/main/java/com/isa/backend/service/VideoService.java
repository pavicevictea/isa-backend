package com.isa.backend.service;

import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.VideoPost;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface VideoService {
    VideoPost createVideoPost(VideoPostUploadDto dto, MultipartFile videoFile, MultipartFile thumbnailFile) throws IOException;
    byte[] getThumbnail(Long videoId) throws IOException;
}

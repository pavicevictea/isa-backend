package com.isa.backend.controller;

import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.VideoPost;
import com.isa.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/api/videos", produces = MediaType.APPLICATION_JSON_VALUE)
public class VideoPostController {

    @Autowired
    private VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadVideo(@RequestPart("data") VideoPostUploadDto dto, @RequestPart("video") MultipartFile videoFile, @RequestPart("thumbnail") MultipartFile thumbnailFile, Principal principal){
        try{
            VideoPost createdPost = videoService.createVideoPost(dto, videoFile, thumbnailFile, principal.getName());
            return ResponseEntity.ok(createdPost);
        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Video upload failed: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    public ResponseEntity<?> getThumbnail(@PathVariable Long id){
        try{
            byte[] image = videoService.getThumbnail(id);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @GetMapping
    public ResponseEntity<List<VideoPost>> getAllVideos() {
        return ResponseEntity.ok(videoService.getAllVideos());
    }

    @GetMapping(value = "/{id}")
    public ResponseEntity<VideoPost> getVideoById(@PathVariable Long id) {
        VideoPost video = videoService.getVideoById(id);
        if (video != null) {
            return ResponseEntity.ok(video);
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping(value = "/{id}/stream")
    public ResponseEntity<Resource> streamVideo(@PathVariable Long id) {
        try {
            VideoPost post = videoService.findOnlyById(id);
            Path path = Paths.get(post.getVideoPath());
            Resource video = new UrlResource(path.toUri());

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "video/mp4"; // Default
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(video);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/view")
    public ResponseEntity<?> incrementView(@PathVariable Long id) {
        videoService.recordView(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping(value = "/{id}/details")
    public ResponseEntity<?> getVideoDetails(@PathVariable Long id, Principal principal){
        try {
            String username = (principal != null) ? principal.getName() : null;
            return ResponseEntity.ok(videoService.getVideoDetails(id, username));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @PostMapping("/{id}/like")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> toggleLike(@PathVariable Long id, Principal principal) {
        try {
            videoService.toggleLike(id, principal.getName());
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Action failed: " + e.getMessage());
        }
    }
}

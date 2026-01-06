package com.isa.backend.controller;

import com.isa.backend.dto.VideoPostUploadDto;
import com.isa.backend.model.VideoPost;
import com.isa.backend.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping(value = "/api/videos", produces = MediaType.APPLICATION_JSON_VALUE)
public class VideoPostController {

    @Autowired
    private VideoService videoService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> uploadVideo(@RequestPart("data") VideoPostUploadDto dto, @RequestPart("video") MultipartFile videoFile, @RequestPart("thumbnail") MultipartFile thumbnailFile){
        try{
            VideoPost createdPost = videoService.createVideoPost(dto, videoFile, thumbnailFile);
            return ResponseEntity.ok(createdPost);
        } catch(Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("Video upload failed: " + e.getMessage());
        }
    }

    @GetMapping(value = "/{id}/thumbnail", produces = MediaType.IMAGE_JPEG_VALUE)
    @PreAuthorize("hasAnyRole('USER')")
    public ResponseEntity<?> getThumbnail(@PathVariable Long id){
        try{
            byte[] image = videoService.getThumbnail(id);
            return ResponseEntity.ok().contentType(MediaType.IMAGE_JPEG).body(image);
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}

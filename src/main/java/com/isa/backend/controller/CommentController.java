package com.isa.backend.controller;

import com.isa.backend.dto.CommentRequestDto;
import com.isa.backend.dto.CommentResponseDto;
import com.isa.backend.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping(value = "/api/comments", produces = MediaType.APPLICATION_JSON_VALUE)
public class CommentController {
    @Autowired
    private CommentService commentService;

    @PostMapping("/video/{videoId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addComment(@PathVariable Long videoId, @RequestBody CommentRequestDto dto, Principal principal) {
        try {
            commentService.addComment(videoId, dto, principal.getName());
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add comment: " + e.getMessage());
        }
    }

    @GetMapping("/video/{videoId}")
    public ResponseEntity<Page<CommentResponseDto>> getVideoComment(@PathVariable Long videoId, @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(commentService.getVideoComments(videoId, pageable));
    }
}

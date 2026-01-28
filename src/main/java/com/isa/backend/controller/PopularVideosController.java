package com.isa.backend.controller;

import com.isa.backend.model.PopularVideos;
import com.isa.backend.service.PopularVideosService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/popular-videos")
public class PopularVideosController {

    @Autowired
    PopularVideosService popularVideosService;

    @GetMapping("/latest")
    public ResponseEntity<PopularVideos> getLatestPopularVideos() {
        PopularVideos latest = popularVideosService.getLatest();

        if (latest != null) {
            return ResponseEntity.ok(latest);
        } else {
            return ResponseEntity.noContent().build();
        }
    }
}

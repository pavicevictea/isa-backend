package com.isa.backend.service;

import com.isa.backend.model.PopularVideos;
import com.isa.backend.model.VideoPost;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface PopularVideosService {
    void runEtl();
    PopularVideos getLatest();
    PopularVideos getLatestByCountry(String country);
    List<VideoPost> getTrendingNearUser(Double lat, Double lon, Double radiusKm);
}

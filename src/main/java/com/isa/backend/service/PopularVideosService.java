package com.isa.backend.service;

import com.isa.backend.model.PopularVideos;
import org.springframework.stereotype.Service;

@Service
public interface PopularVideosService {
    void runEtl();
    PopularVideos getLatest();
    PopularVideos getLatestByCountry(String country);
}

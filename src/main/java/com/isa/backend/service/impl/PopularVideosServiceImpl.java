package com.isa.backend.service.impl;

import com.isa.backend.model.PopularVideos;
import com.isa.backend.model.VideoPost;
import com.isa.backend.model.VideoView;
import com.isa.backend.repository.*;
import com.isa.backend.service.PopularVideosService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PopularVideosServiceImpl implements PopularVideosService {
    private static final Logger logger = LoggerFactory.getLogger(PopularVideosServiceImpl.class);

    @Autowired
    private VideoViewRepository videoViewRepository;

    @Autowired
    private PopularVideosRepository popularVideosRepository;

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private VideoLikeRepository videoLikeRepository;

    @Autowired
    private VideoDislikeRepository videoDislikeRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Override
    @Scheduled(cron = "${etl.cron}")
    public void runEtl() {
        logger.info("ETL proces krenuo");

        List<VideoView> views = extractRecentViews();
        Map<Long, Double> scores = calculateScores(views);
        saveTopThree(scores);

        logger.info("ETL proces gotov");
    }

    private List<VideoView> extractRecentViews() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return videoViewRepository.findAllByViewedAtAfter(sevenDaysAgo);
    }

    private Map<Long, Double> calculateScores(List<VideoView> views){
        Map<Long, Double> scoresMap = new HashMap<>();
        for (VideoView v : views) {
            Long videoId = v.getVideoPost().getId();
            long daysOld = java.time.Duration.between(v.getViewedAt(), LocalDateTime.now()).toDays();

            double weight = 7 - daysOld + 1;
            if (weight < 1) {
                weight = 1.0;
            }

            if (scoresMap.containsKey(videoId)) {
                double currentScore = scoresMap.get(videoId);
                scoresMap.put(videoId, currentScore + weight);
            } else {
                scoresMap.put(videoId, weight);
            }
        }
        return scoresMap;
    }

    private void saveTopThree(Map<Long, Double> scores) {
        List<Map.Entry<Long, Double>> list = new ArrayList<>(scores.entrySet());

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        PopularVideos report = new PopularVideos();
        report.setRunTime(LocalDateTime.now());

        if (list.size() >= 1) {
            fillFirstPlace(report, list.get(0));
        }
        if (list.size() >= 2) {
            fillSecondPlace(report, list.get(1));
        }
        if (list.size() >= 3) {
            fillThirdPlace(report, list.get(2));
        }

        popularVideosRepository.save(report);
    }

    private void fillFirstPlace(PopularVideos report, Map.Entry<Long, Double> entry) {
        VideoPost video = videoPostRepository.findById(entry.getKey()).orElse(null);
        if (video != null) {
            report.setFirstVideoId(video.getId());
            report.setFirstVideoTitle(video.getTitle());
            report.setFirstVideoScore(entry.getValue());
        }
    }

    private void fillSecondPlace(PopularVideos report, Map.Entry<Long, Double> entry) {
        VideoPost video = videoPostRepository.findById(entry.getKey()).orElse(null);
        if (video != null) {
            report.setSecondVideoId(video.getId());
            report.setSecondVideoTitle(video.getTitle());
            report.setSecondVideoScore(entry.getValue());
        }
    }

    private void fillThirdPlace(PopularVideos report, Map.Entry<Long, Double> entry) {
        VideoPost video = videoPostRepository.findById(entry.getKey()).orElse(null);
        if (video != null) {
            report.setThirdVideoId(video.getId());
            report.setThirdVideoTitle(video.getTitle());
            report.setThirdVideoScore(entry.getValue());
        }
    }

    @Override
    public PopularVideos getLatest() {
        return popularVideosRepository.findTopByOrderByRunTimeDesc();
    }

}

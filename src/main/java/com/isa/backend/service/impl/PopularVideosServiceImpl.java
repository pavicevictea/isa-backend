package com.isa.backend.service.impl;

import com.isa.backend.model.*;
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

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        Map<String, Map<Long, Double>> countryScoresMap = new HashMap<>();

        List<VideoView> views = videoViewRepository.findAllByViewedAtAfter(sevenDaysAgo);
        for (VideoView v : views) {
            String country = v.getVideoPost().getLocation().getCountry();
            if (!countryScoresMap.containsKey(country)) {
                countryScoresMap.put(country, new HashMap<>());
            }
            processActivity(countryScoresMap.get(country), v.getVideoPost().getId(), v.getViewedAt(), 1.0);
        }

        List<VideoLike> likes = videoLikeRepository.findAllByCreatedAtAfter(sevenDaysAgo);
        for (VideoLike l : likes) {
            String country = l.getVideo().getLocation().getCountry();
            if (!countryScoresMap.containsKey(country)) {
                countryScoresMap.put(country, new HashMap<>());
            }
            processActivity(countryScoresMap.get(country), l.getVideo().getId(), l.getCreatedAt(), 3.0);
        }

        List<Comment> comments = commentRepository.findAllByCreatedAtAfter(sevenDaysAgo);
        for (Comment c : comments) {
            String country = c.getVideo().getLocation().getCountry();
            if (!countryScoresMap.containsKey(country)) {
                countryScoresMap.put(country, new HashMap<>());
            }
            processActivity(countryScoresMap.get(country), c.getVideo().getId(), c.getCreatedAt(), 5.0);
        }

        List<VideoDislike> dislikes = videoDislikeRepository.findAllByCreatedAtAfter(sevenDaysAgo);
        for (VideoDislike d : dislikes) {
            String country = d.getVideo().getLocation().getCountry();
            if (!countryScoresMap.containsKey(country)) {
                countryScoresMap.put(country, new HashMap<>());
            }
            processActivity(countryScoresMap.get(country), d.getVideo().getId(), d.getCreatedAt(), -2.0);
        }

        for (Map.Entry<String, Map<Long, Double>> entry : countryScoresMap.entrySet()) {
            saveTopThree(entry.getValue(), entry.getKey());
        }

        logger.info("ETL proces gotov");
    }

    private void processActivity(Map<Long, Double> scoresMap, Long videoId, LocalDateTime activityDate, double activityWeight) {
        long daysOld = java.time.Duration.between(activityDate, LocalDateTime.now()).toDays();

        double timeWeight = 7 - daysOld + 1;
        if (timeWeight < 1) {
            timeWeight = 1.0;
        }

        double finalPoints = timeWeight * activityWeight;

        if (scoresMap.containsKey(videoId)) {
            double currentScore = scoresMap.get(videoId);
            scoresMap.put(videoId, currentScore + finalPoints);
        } else {
            scoresMap.put(videoId, finalPoints);
        }
    }

    private void saveTopThree(Map<Long, Double> scores, String countryName) {
        List<Map.Entry<Long, Double>> list = new ArrayList<>(scores.entrySet());

        list.sort((a, b) -> b.getValue().compareTo(a.getValue()));

        PopularVideos report = new PopularVideos();
        report.setRunTime(LocalDateTime.now());
        report.setCountry(countryName);

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

    @Override
    public PopularVideos getLatestByCountry(String country) {
        return popularVideosRepository.findTopByCountryOrderByRunTimeDesc(country);
    }
}
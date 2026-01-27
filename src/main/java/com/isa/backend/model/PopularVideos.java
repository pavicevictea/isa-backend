package com.isa.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="POPULAR_VIDEOS")
public class PopularVideos {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "run_time", nullable = false)
    private LocalDateTime runTime;

    @Column
    private Long firstVideoId;
    @Column
    private String firstVideoTitle;
    @Column
    private Double firstVideoScore;

    @Column
    private Long secondVideoId;
    @Column
    private String secondVideoTitle;
    @Column
    private Double secondVideoScore;

    @Column
    private Long thirdVideoId;
    @Column
    private String thirdVideoTitle;
    @Column
    private Double thirdVideoScore;

    @Column
    private String country;

    public PopularVideos() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getRunTime() { return runTime; }
    public void setRunTime(LocalDateTime runTime) { this.runTime = runTime; }

    public Long getFirstVideoId() { return firstVideoId; }
    public void setFirstVideoId(Long firstVideoId) { this.firstVideoId = firstVideoId; }

    public String getFirstVideoTitle() { return firstVideoTitle; }
    public void setFirstVideoTitle(String firstVideoTitle) { this.firstVideoTitle = firstVideoTitle; }

    public Double getFirstVideoScore() { return firstVideoScore; }
    public void setFirstVideoScore(Double firstVideoScore) { this.firstVideoScore = firstVideoScore; }

    public Long getSecondVideoId() { return secondVideoId; }
    public void setSecondVideoId(Long secondVideoId) { this.secondVideoId = secondVideoId; }

    public String getSecondVideoTitle() { return secondVideoTitle; }
    public void setSecondVideoTitle(String secondVideoTitle) { this.secondVideoTitle = secondVideoTitle; }

    public Double getSecondVideoScore() { return secondVideoScore; }
    public void setSecondVideoScore(Double secondVideoScore) { this.secondVideoScore = secondVideoScore; }

    public Long getThirdVideoId() { return thirdVideoId; }
    public void setThirdVideoId(Long thirdVideoId) { this.thirdVideoId = thirdVideoId; }

    public String getThirdVideoTitle() { return thirdVideoTitle; }
    public void setThirdVideoTitle(String thirdVideoTitle) { this.thirdVideoTitle = thirdVideoTitle; }

    public Double getThirdVideoScore() { return thirdVideoScore; }
    public void setThirdVideoScore(Double thirdVideoScore) { this.thirdVideoScore = thirdVideoScore; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}

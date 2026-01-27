package com.isa.backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="VIDEO_VIEW")
public class VideoView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private VideoPost video;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    public VideoView() { }

    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public VideoPost getVideoPost() { return video; }
    public void setVideo(VideoPost video) { this.video = video; }

    public LocalDateTime getViewedAt() { return viewedAt; }
    public void setViewedAt(LocalDateTime viewedAt) { this.viewedAt = viewedAt; }
}

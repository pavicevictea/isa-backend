package com.isa.backend.model;

import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "VIDEO_LIKES", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "video_id"})
})
public class VideoLike implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private VideoPost video;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public VideoLike() {}

    public VideoLike(User user, VideoPost video){
        this.user = user;
        this.video = video;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public VideoPost getVideo() { return video; }
    public void setVideo(VideoPost video) { this.video = video; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}

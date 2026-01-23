package com.isa.backend.model;

import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "VIDEO_DISLIKES", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "video_id"})
})
public class VideoDislike implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "video_id", nullable = false)
    private VideoPost video;

    public VideoDislike() {}

    public VideoDislike(User user, VideoPost video){
        this.user = user;
        this.video = video;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public VideoPost getVideo() { return video; }
    public void setVideo(VideoPost video) { this.video = video; }
}

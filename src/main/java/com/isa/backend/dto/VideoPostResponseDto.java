package com.isa.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class VideoPostResponseDto {
    private Long id;
    private String title;
    private String description;
    private List<String> tags;
    private String videoPath;
    private LocalDateTime createdAt;
    private String location;
    private Long views;
    private String authorUsername;
    private String firstName;
    private String lastName;

    private Long likesCount;
    private boolean isLikedByCurrentUser;
    private Long dislikesCount;
    private boolean isDislikedByCurrentUser;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public String getVideoPath() { return videoPath; }
    public void setVideoPath(String videoPath) { this.videoPath = videoPath; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public Long getViews() { return views; }
    public void setViews(Long views) { this.views = views; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public Long getLikesCount() { return likesCount; }
    public void setLikesCount(Long likesCount) { this.likesCount = likesCount; }

    public boolean isLikedByCurrentUser() { return isLikedByCurrentUser; }
    public void setLikedByCurrentUser(boolean likedByCurrentUser) { this.isLikedByCurrentUser = likedByCurrentUser; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public Long getDislikesCount() { return dislikesCount; }
    public void setDislikesCount(Long dislikesCount) { this.dislikesCount = dislikesCount; }

    public boolean isDislikedByCurrentUser() { return isDislikedByCurrentUser; }
    public void setDislikedByCurrentUser(boolean dislikedByCurrentUser) { this.isDislikedByCurrentUser = dislikedByCurrentUser; }
}

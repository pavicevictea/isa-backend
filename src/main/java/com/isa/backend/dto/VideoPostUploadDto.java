package com.isa.backend.dto;

import java.util.List;

public class VideoPostUploadDto {
    private String title;
    private String description;
    private List<String> tags;
    private LocationDto location;

    public VideoPostUploadDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public LocationDto getLocation() { return location; }
    public void setLocation(LocationDto location) { this.location = location; }
}

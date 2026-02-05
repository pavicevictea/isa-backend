package com.isa.backend.dto;

public class UploadEventJSON {
    public String title;
    public String author;
    public long size;
    public long durationSeconds;
    public String videoPath;
    public long createdAt;

    public UploadEventJSON() {}

    public UploadEventJSON(String title, String author, long size, long durationSeconds, String videoPath, long createdAt) {
        this.title = title;
        this.author = author;
        this.size = size;
        this.durationSeconds = durationSeconds;
        this.videoPath = videoPath;
        this.createdAt = createdAt;
    }
}

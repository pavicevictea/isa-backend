package com.isa.backend.dto;

public class ChatMessageDTO {
    private String firstName;
    private String lastName;
    private String authorUsername;
    private String text;
    private Long videoId;

    public ChatMessageDTO() {}

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(String authorUsername) { this.authorUsername = authorUsername; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
}

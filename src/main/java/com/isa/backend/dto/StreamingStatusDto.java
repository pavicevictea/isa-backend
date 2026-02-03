package com.isa.backend.dto;

public class StreamingStatusDto {
    private boolean available;
    private long offsetSeconds;
    private String message;

    public StreamingStatusDto(boolean available, long offsetSeconds, String message) {
        this.available = available;
        this.offsetSeconds = offsetSeconds;
        this.message = message;
    }
}
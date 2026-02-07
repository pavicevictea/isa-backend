package com.isa.backend.service;

import com.isa.backend.dto.UploadEventJSON;

public interface MessageBenchmarkService {
    void runComparison() throws Exception;
    void sendUploadEvents(UploadEventJSON event);
}

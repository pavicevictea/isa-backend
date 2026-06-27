package com.isa.backend.integration_tests.config;

import com.isa.backend.service.MessageBenchmarkService;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@TestConfiguration
public class IntegrationTestConfig {

    @Bean
    @Primary
    public SimpMessagingTemplate simpMessagingTemplate() {
        return Mockito.mock(SimpMessagingTemplate.class);
    }

    @Bean
    @Primary
    public MessageBenchmarkService messageBenchmarkService() {
        return Mockito.mock(MessageBenchmarkService.class);
    }
}

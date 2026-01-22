package com.isa.backend.monitoring;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ActiveUsersMetric {

    private final Map<String, Instant> activeUsers = new ConcurrentHashMap<>();

    public ActiveUsersMetric(MeterRegistry registry) {
        Gauge.builder("app_active_users_24h", activeUsers, Map::size)
                .description("Active users in last 24 hours")
                .register(registry);
    }

    public void recordUser(String username) {
        activeUsers.put(username, Instant.now());

        Instant limit = Instant.now().minusSeconds(24 * 3600);
        activeUsers.entrySet()
                .removeIf(e -> e.getValue().isBefore(limit));
    }
}
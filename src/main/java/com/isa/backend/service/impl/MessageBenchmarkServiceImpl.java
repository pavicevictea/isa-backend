package com.isa.backend.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.isa.backend.dto.UploadEventJSON;
import com.isa.backend.dto.UploadEventProto;
import com.isa.backend.service.MessageBenchmarkService;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class MessageBenchmarkServiceImpl implements MessageBenchmarkService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MessageBenchmarkServiceImpl(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void runComparison() throws Exception {
        int iterations = 50;

        UploadEventJSON jsonEvent = new UploadEventJSON(
                "ISA Project",
                "User123",
                1024000L,
                120L,
                "/storage/videos/test.mp4",
                System.currentTimeMillis()
        );

        UploadEventProto.UploadEvent protoEvent = UploadEventProto.UploadEvent.newBuilder()
                .setTitle(jsonEvent.title != null ? jsonEvent.title : "")
                .setAuthor(jsonEvent.author != null ? jsonEvent.author : "")
                .setSize(jsonEvent.size)
                .setDurationSeconds(jsonEvent.durationSeconds)
                .setVideoPath(jsonEvent.videoPath != null ? jsonEvent.videoPath : "")
                .setCreatedAt(jsonEvent.createdAt)
                .build();

        List<Long> jsonSerTimes = new ArrayList<>();
        List<Long> protoSerTimes = new ArrayList<>();
        List<Long> jsonDesTimes = new ArrayList<>();
        List<Long> protoDesTimes = new ArrayList<>();

        byte[] jsonBytes = null;
        byte[] protoBytes = null;

        for (int i = 0; i < iterations; i++) {
            // JSON Serialization
            long start = System.nanoTime();
            jsonBytes = objectMapper.writeValueAsBytes(jsonEvent);
            jsonSerTimes.add(System.nanoTime() - start);

            // JSON Deserialization
            start = System.nanoTime();
            objectMapper.readValue(jsonBytes, UploadEventJSON.class);
            jsonDesTimes.add(System.nanoTime() - start);

            // Protobuf Serialization
            start = System.nanoTime();
            protoBytes = protoEvent.toByteArray();
            protoSerTimes.add(System.nanoTime() - start);

            // Protobuf Deserialization
            start = System.nanoTime();
            UploadEventProto.UploadEvent.parseFrom(protoBytes);
            protoDesTimes.add(System.nanoTime() - start);
        }

        double avgJsonSer = jsonSerTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1000.0;
        double avgProtoSer = protoSerTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1000.0;
        double avgJsonDes = jsonDesTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1000.0;
        double avgProtoDes = protoDesTimes.stream().mapToLong(Long::longValue).average().orElse(0) / 1000.0;

        System.out.println("\n============================================================");
        System.out.println("   JSON VS PROTOBUF COMPARISON (50 MSGS)");
        System.out.println("============================================================");
        System.out.printf("%-25s | %-12s | %-12s%n", "Parameter", "JSON", "Protobuf");
        System.out.println("------------------------------------------------------------");
        System.out.printf("%-25s | %-12d | %-12d%n", "Message Size (Bytes)", jsonBytes.length, protoBytes.length);
        System.out.printf("%-25s | %-12.2f | %-12.2f%n", "Avg Serialization (µs)", avgJsonSer, avgProtoSer);
        System.out.printf("%-25s | %-12.2f | %-12.2f%n", "Avg Deserialization (µs)", avgJsonDes, avgProtoDes);
        System.out.println("============================================================\n");
    }

    @Override
    public void sendUploadEvents(UploadEventJSON event) {
        try {
            String jsonPayload = objectMapper.writeValueAsString(event);
            rabbitTemplate.convertAndSend("video.exchange", "video.json", jsonPayload);
            System.out.println("MQ: JSON event sent successfully as String.");
        } catch (Exception e) {
            System.err.println("MQ Error (JSON): " + e.getMessage());
        }

        try {
            UploadEventProto.UploadEvent protoEvent = UploadEventProto.UploadEvent.newBuilder()
                    .setTitle(event.title != null ? event.title : "")
                    .setAuthor(event.author != null ? event.author : "")
                    .setSize(event.size)
                    .setDurationSeconds(event.durationSeconds)
                    .setVideoPath(event.videoPath != null ? event.videoPath : "")
                    .setCreatedAt(event.createdAt)
                    .build();

            rabbitTemplate.convertAndSend("video.exchange", "video.proto", protoEvent.toByteArray());
            System.out.println("MQ: Protobuf event sent successfully.");
        } catch (Exception e) {
            System.err.println("MQ Error (Protobuf): " + e.getMessage());
        }
    }
}
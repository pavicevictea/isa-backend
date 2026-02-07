package com.isa.backend.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE = "video.exchange";
    public static final String JSON_QUEUE = "video.json.queue";
    public static final String PROTO_QUEUE = "video.proto.queue";
    public static final String JSON_ROUTING_KEY = "video.json";
    public static final String PROTO_ROUTING_KEY = "video.proto";

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange(EXCHANGE);
    }

    @Bean
    public Queue jsonQueue() {
        return new Queue(JSON_QUEUE);
    }

    @Bean
    public Queue protoQueue() {
        return new Queue(PROTO_QUEUE);
    }

    @Bean
    public Binding jsonBinding(Queue jsonQueue, TopicExchange videoExchange) {
        return BindingBuilder.bind(jsonQueue).to(videoExchange).with(JSON_ROUTING_KEY);
    }

    @Bean
    public Binding protoBinding(Queue protoQueue, TopicExchange videoExchange) {
        return BindingBuilder.bind(protoQueue).to(videoExchange).with(PROTO_ROUTING_KEY);
    }
}
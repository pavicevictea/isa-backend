package com.isa.backend.controller;

import com.isa.backend.dto.ChatMessageDTO;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @MessageMapping("/send-message/{videoId}")
    @SendTo("/socket-publisher/video/{videoId}")
    public ChatMessageDTO broadcastMessage(@DestinationVariable Long videoId, @Payload ChatMessageDTO message) {
        return message;
    }
}

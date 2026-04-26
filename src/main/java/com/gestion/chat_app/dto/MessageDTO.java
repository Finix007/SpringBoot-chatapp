package com.gestion.chat_app.dto;

import com.gestion.chat_app.entities.Message;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String content;
    private String imageUrl;
    private Message.MessageType type;
    private LocalDateTime sentAt;
    private Long chatRoomId;

    // Incoming WebSocket message payload
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Incoming {
        private String content;
        private String type; // "TEXT" or "IMAGE"
        private String imageUrl;
    }
}

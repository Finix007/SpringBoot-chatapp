package com.gestion.chat_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TypingEvent {
    private Long userId;
    private String username;
    private boolean typing; // true = started, false = stopped
    private Long chatRoomId;
}

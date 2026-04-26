package com.gestion.chat_app.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomDTO {
    private Long id;
    private Long otherUserId;
    private String otherUsername;
    private String otherUserPublicId;
    private String lastMessage;
    private String roomKey;
}
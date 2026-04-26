package com.gestion.chat_app.controllers;

import com.gestion.chat_app.Services.IChatRoomService;
import com.gestion.chat_app.Services.IMessageService;
import com.gestion.chat_app.Services.IUserService;
import com.gestion.chat_app.dto.MessageDTO;
import com.gestion.chat_app.dto.TypingEvent;
import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.Message;
import com.gestion.chat_app.entities.User;
import lombok.AllArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@AllArgsConstructor

public class WebSocketMessageController {

    IUserService userService;
    IChatRoomService chatRoomService;
    IMessageService messageService;
    SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId,
                            @Payload MessageDTO.Incoming payload,
                            Principal principal) {
        User sender = userService.findByEmail(principal.getName()).orElseThrow();
        ChatRoom room = chatRoomService.findById(roomId).orElseThrow();

        if (!chatRoomService.isParticipant(room, sender)) return;

        Message saved;
        if ("IMAGE".equalsIgnoreCase(payload.getType())) {
            saved = messageService.saveImageMessage(room, sender, payload.getImageUrl());
        } else {
            if (payload.getContent() == null || payload.getContent().isBlank()) return;
            saved = messageService.saveTextMessage(room, sender, payload.getContent().trim());
        }

        MessageDTO dto = messageService.toDTO(saved);
        // Broadcast to all subscribers of this room's topic
        messagingTemplate.convertAndSend("/topic/chat/" + roomId, dto);
    }

    @MessageMapping("/chat/{roomId}/typing")
    public void handleTyping(@DestinationVariable Long roomId,
                             @Payload TypingEvent event,
                             Principal principal) {
        User sender = userService.findByEmail(principal.getName()).orElseThrow();
        ChatRoom room = chatRoomService.findById(roomId).orElse(null);
        if (room == null || !chatRoomService.isParticipant(room, sender)) return;

        event.setUserId(sender.getId());
        event.setUsername(sender.getUsername());
        event.setChatRoomId(roomId);

        // Broadcast typing signal to the room (all participants including sender)
        messagingTemplate.convertAndSend("/topic/chat/" + roomId + "/typing", event);
    }

}


package com.gestion.chat_app.Services;

import com.gestion.chat_app.dto.MessageDTO;
import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.Message;
import com.gestion.chat_app.entities.User;

import java.util.List;

public interface IMessageService {
    Message saveTextMessage(ChatRoom room, User sender, String content);
    Message saveImageMessage(ChatRoom room, User sender, String imageUrl);
    List<MessageDTO> getRoomMessages(ChatRoom room);
    MessageDTO toDTO(Message msg);
}

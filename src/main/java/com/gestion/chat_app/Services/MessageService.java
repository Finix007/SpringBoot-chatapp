package com.gestion.chat_app.Services;

import com.gestion.chat_app.dto.MessageDTO;
import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.Message;
import com.gestion.chat_app.entities.User;
import com.gestion.chat_app.repositories.MessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class MessageService implements IMessageService{

    MessageRepository messageRepository;

    @Override
    public Message saveTextMessage(ChatRoom room, User sender, String content) {
        Message msg = Message.builder()
                .chatRoom(room)
                .sender(sender)
                .content(content)
                .type(Message.MessageType.TEXT)
                .build();
        return messageRepository.save(msg);
    }

    @Override
    public Message saveImageMessage(ChatRoom room, User sender, String imageUrl) {
        Message msg = Message.builder()
                .chatRoom(room)
                .sender(sender)
                .imageUrl(imageUrl)
                .type(Message.MessageType.IMAGE)
                .build();
        return messageRepository.save(msg);
    }

    @Override
    public List<MessageDTO> getRoomMessages(ChatRoom room) {
        return messageRepository.findByChatRoomOrderBySentAtAsc(room)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MessageDTO toDTO(Message msg) {
        return MessageDTO.builder()
                .id(msg.getId())
                .senderId(msg.getSender().getId())
                .senderUsername(msg.getSender().getUsername())
                .content(msg.getContent())
                .imageUrl(msg.getImageUrl())
                .type(msg.getType())
                .sentAt(msg.getSentAt())
                .chatRoomId(msg.getChatRoom().getId())
                .build();
    }
}

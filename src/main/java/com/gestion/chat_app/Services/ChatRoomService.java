package com.gestion.chat_app.Services;

import com.gestion.chat_app.dto.ChatRoomDTO;
import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.Message;
import com.gestion.chat_app.entities.User;
import com.gestion.chat_app.repositories.ChatRoomRepository;
import com.gestion.chat_app.repositories.MessageRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ChatRoomService implements IChatRoomService{

    ChatRoomRepository chatRoomRepository;
    MessageRepository messageRepository;

    @Override
    public ChatRoom getOrCreateRoom(User user1, User user2) {
        String key = ChatRoom.buildRoomKey(user1.getId(), user2.getId());
        return chatRoomRepository.findByRoomKey(key).orElseGet(() -> {
            ChatRoom room = ChatRoom.builder()
                    .user1(user1)
                    .user2(user2)
                    .roomKey(key)
                    .build();
            return chatRoomRepository.save(room);
        });
    }

    @Override
    public Optional<ChatRoom> findById(Long id) {
        return chatRoomRepository.findById(id);
    }

    @Override
    public List<ChatRoomDTO> getUserRooms(User currentUser) {
        return chatRoomRepository.findAllByUser(currentUser).stream()
                .map(room -> {
                    User other = room.getUser1().getId().equals(currentUser.getId())
                            ? room.getUser2() : room.getUser1();

                    List<Message> messages = messageRepository.findByChatRoomOrderBySentAtAsc(room);
                    String lastMsg = messages.isEmpty() ? "No messages yet" :
                            messages.get(messages.size() - 1).getType() == Message.MessageType.IMAGE
                                    ? "📷 Image" : messages.get(messages.size() - 1).getContent();

                    return ChatRoomDTO.builder()
                            .id(room.getId())
                            .otherUserId(other.getId())
                            .otherUsername(other.getUsername())
                            .otherUserPublicId(other.getPublicId())
                            .lastMessage(lastMsg)
                            .roomKey(room.getRoomKey())
                            .build();
                }).collect(Collectors.toList());
    }

    @Override
    public boolean isParticipant(ChatRoom room, User user) {
        return room.getUser1().getId().equals(user.getId())
                || room.getUser2().getId().equals(user.getId());
    }
}

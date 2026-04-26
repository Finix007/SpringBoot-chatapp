package com.gestion.chat_app.Services;

import com.gestion.chat_app.dto.ChatRoomDTO;
import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.User;

import java.util.List;
import java.util.Optional;

public interface IChatRoomService {
    ChatRoom getOrCreateRoom(User user1, User user2);
    Optional<ChatRoom> findById(Long id);
    List<ChatRoomDTO> getUserRooms(User currentUser);
    boolean isParticipant(ChatRoom room, User user);
}

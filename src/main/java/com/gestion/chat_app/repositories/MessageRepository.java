package com.gestion.chat_app.repositories;

import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatRoomOrderBySentAtAsc(ChatRoom chatRoom);
}
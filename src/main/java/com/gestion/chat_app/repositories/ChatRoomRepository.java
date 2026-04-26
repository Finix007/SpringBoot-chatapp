package com.gestion.chat_app.repositories;

import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByRoomKey(String roomKey);

    @Query("SELECT cr FROM ChatRoom cr WHERE cr.user1 = :user OR cr.user2 = :user ORDER BY cr.createdAt DESC")
    List<ChatRoom> findAllByUser(@Param("user") User user);
}

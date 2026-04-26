package com.gestion.chat_app.controllers;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.gestion.chat_app.Services.IChatRoomService;
import com.gestion.chat_app.Services.IMessageService;
import com.gestion.chat_app.Services.IUserService;
import com.gestion.chat_app.dto.MessageDTO;
import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.User;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Controller
@AllArgsConstructor
public class ChatController {

    IUserService userService;
    IChatRoomService chatRoomService;
    IMessageService messageService;
    Cloudinary cloudinary;


    @GetMapping("/chat/{roomId}")
    public String chatRoom(@PathVariable Long roomId,
                           @AuthenticationPrincipal UserDetails userDetails,
                           Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        ChatRoom room = chatRoomService.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Chat room not found"));

        if (!chatRoomService.isParticipant(room, currentUser)) {
            return "redirect:/dashboard";
        }

        User other = room.getUser1().getId().equals(currentUser.getId())
                ? room.getUser2() : room.getUser1();

        List<MessageDTO> messages = messageService.getRoomMessages(room);

        model.addAttribute("room", room);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherUser", other);
        model.addAttribute("messages", messages);
        return "room";
    }

    // REST: Get messages for a room
    @GetMapping("/api/chat/{roomId}/messages")
    @ResponseBody
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long roomId,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userService.findByEmail(userDetails.getUsername()).orElseThrow();
        ChatRoom room = chatRoomService.findById(roomId).orElseThrow();
        if (!chatRoomService.isParticipant(room, currentUser)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(messageService.getRoomMessages(room));
    }

    // REST: Upload image to Cloudinary and return URL
    @PostMapping("/api/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());

            String url = uploadResult.get("url").toString();
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IOException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Upload failed"));
        }
    }
}

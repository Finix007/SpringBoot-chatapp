package com.gestion.chat_app.controllers;

import com.gestion.chat_app.Services.IChatRoomService;
import com.gestion.chat_app.Services.IUserService;
import com.gestion.chat_app.dto.ChatRoomDTO;
import com.gestion.chat_app.entities.ChatRoom;
import com.gestion.chat_app.entities.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@AllArgsConstructor
public class DashboardController {

    IUserService userService;
    IChatRoomService chatRoomService;


    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userService.findByEmail(userDetails.getUsername())
                .orElseThrow();
        List<ChatRoomDTO> rooms = chatRoomService.getUserRooms(currentUser);

        model.addAttribute("user", currentUser);
        model.addAttribute("rooms", rooms);
        return "dashboard";
    }

    @PostMapping("/chat/create")
    public String createChat(@AuthenticationPrincipal UserDetails userDetails,
                             @RequestParam String targetPublicId,
                             RedirectAttributes redirectAttributes) {
        User currentUser = userService.findByEmail(userDetails.getUsername()).orElseThrow();

        if (targetPublicId.equalsIgnoreCase(currentUser.getPublicId())) {
            redirectAttributes.addFlashAttribute("error", "You can't chat with yourself!");
            return "redirect:/dashboard";
        }

        return userService.findByPublicId(targetPublicId)
                .map(target -> {
                    ChatRoom room = chatRoomService.getOrCreateRoom(currentUser, target);
                    return "redirect:/chat/" + room.getId();
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("error", "User not found with ID: " + targetPublicId);
                    return "redirect:/dashboard";
                });
    }
}


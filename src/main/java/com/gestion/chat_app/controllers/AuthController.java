package com.gestion.chat_app.controllers;

import com.gestion.chat_app.Services.IUserService;
import com.gestion.chat_app.Services.RegisterService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    IUserService userService;
    RegisterService registerService;

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String logout,
                            Model model) {
        if (error != null) model.addAttribute("error", "Invalid username or password.or Email unable");
        if (logout != null) model.addAttribute("logout", "You have been logged out.");
        return "login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        try {
            registerService.register(username, email, password);
            model.addAttribute("email", email);
            return "mail-envoye"; // → templates/mail-envoye.html

        } catch (IllegalStateException e) {
            // Erreur de validation (email/username déjà pris)
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        } catch (Exception e) {
            // Erreur d'envoi email ou autre
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/auth/register";
        }
    }

    @GetMapping("/confirmer")
    public String confirmer(@RequestParam String token, Model model) {
        try {
            registerService.confirmToken(token);
            return "inscription-confirmee"; // → templates/inscription-confirmee.html

        } catch (IllegalStateException e) {
            model.addAttribute("erreur", e.getMessage());
            return "inscription-confirmee";
        }
    }

}

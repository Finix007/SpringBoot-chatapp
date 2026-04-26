package com.gestion.chat_app.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "confirmation_token")
public class ConfirmationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * La valeur UUID du token, envoyée dans le lien de confirmation par email.
     */
    @Column(nullable = false, unique = true)
    private String token;

    /**
     * Date/heure de création du token.
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;


    @OneToOne
    @JoinColumn(name = "users_id", nullable = false)
    private User user;
}
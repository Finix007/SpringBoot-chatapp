package com.gestion.chat_app.entities;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;


    @Column(unique = true, nullable = false, length = 50)
    private String email;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false, length = 36)
    private String publicId;

    @Column(nullable = false)
    private boolean enabled = false;

    @PrePersist
    public void generatePublicId() {
        if (this.publicId == null) {
            this.publicId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        }
    }
}
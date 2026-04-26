package com.gestion.chat_app.Services;

import com.gestion.chat_app.entities.ConfirmationToken;
import com.gestion.chat_app.entities.User;
import com.gestion.chat_app.repositories.ConfirmationTokenRepository;
import com.gestion.chat_app.repositories.UserRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegisterService {

        private static final long TOKEN_EXPIRATION_MINUTES = 15;

    private final UserRepository appUserRepository;

    private final ConfirmationTokenRepository confirmationTokenRepository;

    private final EmailService emailService;

    private final  PasswordEncoder passwordEncoder;

        // Uncomment this when you add Spring Security!
        // @Autowired
        // private BCryptPasswordEncoder passwordEncoder;

        @Value("${app.base-url}")
        private String baseUrl;

        // Remove @Transactional from here so the email doesn't send before DB commit
        public void register(String username, String email, String password) {

            // 1. Do DB operations inside a localized transaction
            String tokenValue = saveUserAndToken(username, email, password);

            // 2. ONLY send email after we are 100% sure the DB saved successfully
            String confirmationLink = baseUrl + "/auth/confirmer?token=" + tokenValue;
            String emailBody = buildEmailBody(username, confirmationLink);
            emailService.send(email, "Confirmez votre inscription", emailBody);
        }

    @Transactional
        public String saveUserAndToken(String username, String email, String password) {
            if (appUserRepository.existsByEmail(email)) {
                throw new IllegalStateException("Cet email est déjà utilisé : " + email);
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            appUserRepository.save(user);

            String tokenValue = UUID.randomUUID().toString();
            LocalDateTime now = LocalDateTime.now();

            ConfirmationToken confirmationToken = new ConfirmationToken();
            confirmationToken.setToken(tokenValue);
            confirmationToken.setCreatedAt(now);
            confirmationToken.setExpiresAt(now.plusMinutes(TOKEN_EXPIRATION_MINUTES));
            confirmationToken.setUser(user);

            confirmationTokenRepository.save(confirmationToken);

            return tokenValue;
        }

    @Transactional(noRollbackFor = IllegalStateException.class)
        public void confirmToken(String token) {
            ConfirmationToken confirmationToken = confirmationTokenRepository
                    .findByToken(token)
                    .orElseThrow(() -> new IllegalStateException("Token invalide : " + token));

            User user = confirmationToken.getUser();

            if (LocalDateTime.now().isAfter(confirmationToken.getExpiresAt())) {
                confirmationTokenRepository.delete(confirmationToken);
                appUserRepository.deleteById(user.getId());
                throw new IllegalStateException("Le token a expiré. Veuillez vous réinscrire.");
            }

            user.setEnabled(true);
            appUserRepository.save(user);

            // If you get a foreign key error here, remove this line and use a "confirmedAt" field instead!
            confirmationTokenRepository.delete(confirmationToken);
        }

        private String buildEmailBody(String username, String link) {
            return """
                <div style="font-family: Arial, sans-serif; max-width: 600px; margin: auto; padding: 24px;
                            border: 1px solid #e0e0e0; border-radius: 8px;">
                    <h2 style="color: #2d6a4f;">Bonjour %s 👋</h2>
                    <p>Merci de vous être inscrit sur notre application.</p>
                    <p>Cliquez sur le bouton ci-dessous pour confirmer votre inscription.
                       Ce lien est valide pendant <strong>15 minutes</strong>.</p>
                    <a href="%s"
                       style="display: inline-block; margin-top: 16px; padding: 12px 24px;
                              background-color: #2d6a4f; color: white; text-decoration: none;
                              border-radius: 6px; font-weight: bold;">
                        ✅ Confirmer mon inscription
                    </a>
                    <p style="margin-top: 24px; color: #888; font-size: 12px;">
                        Si vous n'êtes pas à l'origine de cette inscription, ignorez cet email.
                    </p>
                </div>
                """.formatted(username, link);
        }

        public Optional<User> loadAppUserByUserName(String email)
        {
            return appUserRepository.findByEmail(email);
        }

    }

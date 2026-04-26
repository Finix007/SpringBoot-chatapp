package com.gestion.chat_app.Services;

import com.gestion.chat_app.entities.User;
import com.gestion.chat_app.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public class UserService implements IUserService {

     UserRepository userRepository;
     PasswordEncoder passwordEncoder;

    @Override
    public User register(String username, String email,String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already taken");
        }
        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByPublicId(String publicId) {
        return userRepository.findByPublicId(publicId);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
}
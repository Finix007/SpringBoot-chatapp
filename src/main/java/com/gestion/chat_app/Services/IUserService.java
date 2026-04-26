package com.gestion.chat_app.Services;

import com.gestion.chat_app.entities.User;

import java.util.Optional;

public interface IUserService {
    User register(String username,String email , String password);
    Optional<User> findByEmail(String email);
    Optional<User> findByPublicId(String publicId);
    Optional<User> findById(Long id);
}

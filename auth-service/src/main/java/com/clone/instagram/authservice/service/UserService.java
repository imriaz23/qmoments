package com.clone.instagram.authservice.service;

import com.clone.instagram.authservice.model.User;

import java.util.List;
import java.util.Optional;


public interface UserService {
    public List<User> findAll();

    public Optional<User> findByUsername(String username);

    public List<User> findByUsernameIn(List<String> usernames);

    public User registerUser(User user);

    public User updateProfilePicture(String uri, String id);
}

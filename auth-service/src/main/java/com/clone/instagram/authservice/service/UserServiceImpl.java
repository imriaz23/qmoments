package com.clone.instagram.authservice.service;

import com.clone.instagram.authservice.exception.EmailAlreadyExistsException;
import com.clone.instagram.authservice.exception.ResourceNotFoundException;
import com.clone.instagram.authservice.exception.UsernameAlreadyExistsException;
import com.clone.instagram.authservice.messaging.UserEventSender;
import com.clone.instagram.authservice.model.Role;
import com.clone.instagram.authservice.model.User;
import com.clone.instagram.authservice.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Slf4j
@Service
public class UserServiceImpl implements UserService {


  private UserRepository userRepository;

  private PasswordEncoder passwordEncoder;

  private UserEventSender userEventSender;

  @Autowired
  public UserServiceImpl(@Lazy UserRepository userRepository,
      @Lazy PasswordEncoder passwordEncoder, UserEventSender userEventSender) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
    this.userEventSender = userEventSender;
  }

  @Override
  public List<User> findAll() {
    log.info("retrieving all the users");
    return userRepository.findAll();
  }

  @Override

  public Optional<User> findByUsername(String username) {
    log.info("retrieving user {}", username);
    return userRepository.findByUsername(username);
  }

  @Override
  public List<User> findByUsernameIn(List<String> usernames) {
    log.info("retrieving users {}", usernames);
    return userRepository.findByUsernameIn(usernames);
  }

  @Override
  public User registerUser(User user) {
    log.info("registering user {}", user.getUsername());
    if (userRepository.existsByUsername(user.getUsername())) {
      log.warn("username {} already exists.", user.getUsername());

      throw new UsernameAlreadyExistsException(
          String.format("username %s already exists", user.getUsername()));
    }
    if (userRepository.existsByEmail((user.getEmail()))) {
      log.warn("email {} already exists.", user.getEmail());

      throw new EmailAlreadyExistsException(
          String.format("emial %s already exists", user.getEmail()));
    }

    user.setActive(true);
    user.setPassword(passwordEncoder.encode(user.getPassword()));
    user.setRoles(new HashSet<>() {{
      add(Role.USER);
    }});
    User savedUser = userRepository.save(user);
    userEventSender.sendUserCreated(savedUser);
    log.info("username {} saved into to the database", savedUser.getUsername());
    return savedUser;
  }

  @Override
  public User updateProfilePicture(String uri, String id) {
    log.info("update the profile picture {} for user {} ", uri, id);

    return userRepository
        .findById(id)
        .map(user -> {
          String oldProfilePicture = user.getUserProfile().getProfilePictureUrl();
          user.getUserProfile().setProfilePictureUrl(uri);
          User savedUser = userRepository.save(user);

          return savedUser;
        })
        .orElseThrow(
            () -> new ResourceNotFoundException(String.format("user id %s not found", id)));
  }
}

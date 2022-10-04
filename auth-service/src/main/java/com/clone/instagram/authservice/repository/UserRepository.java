package com.clone.instagram.authservice.repository;

import com.clone.instagram.authservice.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String userName);

    List<User> findByUsernameIn(List<String> usernames);

    boolean existsByUsername(String usernames);

    boolean existsByEmail(String email);
}

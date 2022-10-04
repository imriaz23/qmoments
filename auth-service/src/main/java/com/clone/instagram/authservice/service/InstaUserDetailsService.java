package com.clone.instagram.authservice.service;

import com.clone.instagram.authservice.model.InstaUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class InstaUserDetailsService implements UserDetailsService {

  private UserService userService;

  @Autowired
  public InstaUserDetailsService(@Lazy UserService userService) {
    this.userService = userService;
  }

  @Override
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    return userService
        .findByUsername(username)
        .map(InstaUserDetails::new)
        .orElseThrow(() -> new UsernameNotFoundException("Username not found"));

  }
}

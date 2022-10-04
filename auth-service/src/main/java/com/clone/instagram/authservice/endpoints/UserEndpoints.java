package com.clone.instagram.authservice.endpoints;

import com.clone.instagram.authservice.exception.BadRequestException;
import com.clone.instagram.authservice.exception.EmailAlreadyExistsException;
import com.clone.instagram.authservice.exception.ResourceNotFoundException;
import com.clone.instagram.authservice.exception.UsernameAlreadyExistsException;
import com.clone.instagram.authservice.model.InstaUserDetails;
import com.clone.instagram.authservice.model.Profile;
import com.clone.instagram.authservice.model.User;
import com.clone.instagram.authservice.payload.ApiResponse;
import com.clone.instagram.authservice.payload.JwtAuthenticationResponse;
import com.clone.instagram.authservice.payload.LoginRequest;
import com.clone.instagram.authservice.payload.SignUpRequest;
import com.clone.instagram.authservice.payload.UserSummary;
import com.clone.instagram.authservice.service.JwtTokenProvider;
import com.clone.instagram.authservice.service.UserService;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@Slf4j
public class UserEndpoints {

  @Autowired
  private AuthenticationManager authenticationManager;
  @Autowired
  private JwtTokenProvider jwtTokenProvider;
  @Autowired
  private UserService userService;

  @PostMapping("/signin")
  public ResponseEntity<?> authenticateUser(
      @Valid @RequestBody LoginRequest loginRequest) {
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            loginRequest.getUsername(), loginRequest.getPassword()
        )
    );
    SecurityContextHolder.getContext().setAuthentication(authentication);
    String jwt = jwtTokenProvider.generateToken(authentication);
    log.info("user {} logged in successfully", loginRequest.getUsername());
    return ResponseEntity.ok(new JwtAuthenticationResponse(jwt));
  }


  @PostMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> createUser(@Valid @RequestBody SignUpRequest signUpRequest) {
    log.info("creating user {}", signUpRequest.getUsername());
    User user = User
        .builder()
        .username(signUpRequest.getUsername())
        .email(signUpRequest.getEmail())
        .password(signUpRequest.getPassword())
        .userProfile(Profile.builder()
            .displayName(signUpRequest.getName())
            .build())
        .build();
    try {
      userService.registerUser(user);
    } catch (UsernameAlreadyExistsException | EmailAlreadyExistsException e) {
      throw new BadRequestException(e.getMessage());
    }
    URI location = ServletUriComponentsBuilder
        .fromCurrentContextPath().path("/users/{username}")
        .buildAndExpand(user.getUsername()).toUri();
    return ResponseEntity
        .created(location)
        .body(new ApiResponse(true, "User registered successfully"));
  }

  @PutMapping("/users/me/picture")
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<?> updateProfilePicture(@RequestBody String profilePicture,
      @AuthenticationPrincipal InstaUserDetails userDetails) {
    userService.updateProfilePicture(profilePicture, userDetails.getId());
    return ResponseEntity
        .ok()
        .body(new ApiResponse(true, "Profile picture successfully updated"));
  }

  @GetMapping(value = "/users/{username}", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findUser(@PathVariable("username") String username) {
    log.info("Retrieving user {} ", username);
    return userService
        .findByUsername(username)
        .map(user -> ResponseEntity.ok(user))
        .orElseThrow(() -> new ResourceNotFoundException(username));

  }

  @GetMapping(value = "/users", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findAll() {
    log.info("Retrieving all users ");
    return ResponseEntity.ok(userService.findAll());
  }

  @GetMapping(value = "/users/me", produces = MediaType.APPLICATION_JSON_VALUE)
  @PreAuthorize("hasRole('USER')")
  @ResponseStatus(HttpStatus.OK)
  public UserSummary getCurrentUser(@AuthenticationPrincipal InstaUserDetails userDetails) {
    log.info("Retrieving current user {} ", userDetails.getUsername());
    UserSummary userSummary = UserSummary
        .builder()
        .id(userDetails.getId())
        .username(userDetails.getUsername())
        .profilePicture(userDetails.getUserProfile().getProfilePictureUrl())
        .build();
    return userSummary;
  }

  @GetMapping(value = "/users/summary/{username}", produces = MediaType.APPLICATION_JSON_VALUE)

  public ResponseEntity<?> getUserSummary(@PathVariable("username") String username) {
    log.info("Retrieving user {} summary", username);
    return userService.findByUsername(username)
        .map(user -> ResponseEntity.ok(convertTo(user)))
        .orElseThrow(() -> new ResourceNotFoundException(username));
  }

  @GetMapping(value = "/users/summary/in", produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> getUserSummaries(
      @RequestBody List<String> usernames) {
    log.info("Retrieving summaries for {} usernames", usernames.size());
    List<UserSummary> userSummaries = userService
        .findByUsernameIn(usernames)
        .stream()
        .map(user -> convertTo(user))
        .collect(Collectors.toList());
    return ResponseEntity.ok(userSummaries);

  }

  private UserSummary convertTo(User user) {
    return UserSummary
        .builder()
        .id(user.getId())
        .name(user.getUserProfile().getDisplayName())
        .username(user.getUsername())
        .profilePicture(user.getUserProfile().getProfilePictureUrl())
        .build();
  }
}

package com.clone.instagram.authservice.messaging;

import com.clone.instagram.authservice.model.User;
import com.clone.instagram.authservice.payload.UserEventPayload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserEventSender {

  private UserEventChannel userEventChannel;

  public UserEventSender(UserEventChannel userEventChannel) {
    this.userEventChannel = userEventChannel;
  }

  public void sendUserCreated(User user) {
    log.info("sending user created event for User {} ", user.getUsername());
    sendUserChangedEvent(convertTo(user, UserEventType.CREATED));
  }

  public void sendUserChangedEvent(UserEventPayload payload) {
    Message<UserEventPayload> message = MessageBuilder
        .withPayload(payload)
        .setHeader(KafkaHeaders.MESSAGE_KEY, payload.getId())
        .build();
    userEventChannel.momentsUserChanged().send(message);
    log.info("user event {} sent to topic {} for User {} ",
        message.getPayload().getEventType().name(), userEventChannel.OUTPUT,
        message.getPayload().getUsername());

  }

  public UserEventPayload convertTo(User user, UserEventType eventType) {
    return UserEventPayload
        .builder()
        .eventType(eventType)
        .id(user.getId())
        .username(user.getUsername())
        .email(user.getEmail())
        .displayName(user.getUserProfile().getDisplayName())
        .profilePictureUrl(user.getUserProfile().getProfilePictureUrl())
        .build();
  }
}

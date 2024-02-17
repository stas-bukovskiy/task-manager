package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.UserInvitedEvent;
import org.tasker.updates.models.response.NotificationResponse;
import org.tasker.updates.service.NotificationService;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    @Override
    public NotificationResponse<?> processEvent(Event event) {
        if (event.getEventType().equals(UserInvitedEvent.USER_INVITED_V1)) {
            final var userInvitedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class);
            return processUserInvitedEvent(userInvitedEvent);
        } else {
            log.warn("Unknown event type: {}", event);
            return null;
        }
    }

    private NotificationResponse<?> processUserInvitedEvent(UserInvitedEvent baseEvent) {
        log.info("Processing user invited event: {}", baseEvent);
        return NotificationResponse.builder()
                .toUserId(baseEvent.getToUserId())
                .type(UserInvitedEvent.USER_INVITED_V1)
                .message("You have been invited to board: " + baseEvent.getBoardTitle() + " by " + baseEvent.getFromUserName())
                .build();
    }
}

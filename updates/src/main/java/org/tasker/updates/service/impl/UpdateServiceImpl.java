package org.tasker.updates.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.BoardCreatedEvent;
import org.tasker.common.models.event.UserInvitedEvent;
import org.tasker.updates.models.response.UpdateResponse;
import org.tasker.updates.service.UpdateService;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UpdateServiceImpl implements UpdateService {

    @Override
    public List<UpdateResponse<?>> processEvent(Event event) {
        if (event.getEventType().equals(UserInvitedEvent.USER_INVITED_V1)) {
            final var userInvitedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class);
            return processUserInvitedEvent(userInvitedEvent);
        } else if (event.getEventType().equals(BoardCreatedEvent.BOARD_CREATED_V1)) {
            final var boardCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardCreatedEvent.class);
            return processBoardCreatedEvent(boardCreatedEvent);
        } else {
            log.warn("Unknown event type: {}", event);
            return List.of();
        }
    }

    private List<UpdateResponse<?>> processBoardCreatedEvent(BoardCreatedEvent boardCreatedEvent) {
        log.info("Processing board created event: {}", boardCreatedEvent);
        return Collections.singletonList(UpdateResponse.builder()
                .toUserId(boardCreatedEvent.getOwnerId())
                .updateType(UpdateResponse.UpdateType.UPDATE)
                .type(BoardCreatedEvent.BOARD_CREATED_V1)
                .data(boardCreatedEvent)
                .build());
    }

    private List<UpdateResponse<?>> processUserInvitedEvent(UserInvitedEvent baseEvent) {
        log.info("Processing user invited event: {}", baseEvent);
        return List.of(
                UpdateResponse.builder()
                        .toUserId(baseEvent.getToUserId())
                        .type(UserInvitedEvent.USER_INVITED_V1)
                        .updateType(UpdateResponse.UpdateType.NOTIFICATION)
                        .message("You have been invited to board: " + baseEvent.getBoardTitle() + " by " + baseEvent.getFromUserName())
                        .build(),
                UpdateResponse.builder()
                        .toUserId(baseEvent.getFromUserId())
                        .type(UserInvitedEvent.USER_INVITED_V1)
                        .updateType(UpdateResponse.UpdateType.UPDATE)
                        .data(baseEvent)
                        .build());
    }
}

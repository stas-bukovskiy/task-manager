package org.tasker.common.models.domain;

import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.*;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class BoardAggregate extends AggregateRoot {

    public static final String AGGREGATE_TYPE = "board_aggregate";

    private String title;
    private String ownerId;
    private Set<String> invitedIds;
    private Set<String> joinedIds;
    private boolean isDeleted;

    public BoardAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case BoardCreatedEvent.BOARD_CREATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardCreatedEvent.class));
            case BoardDeletedEvent.BOARD_DELETED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardDeletedEvent.class));
            case BoardMemberDeletedEvent.BOARD_MEMBER_DELETED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardMemberDeletedEvent.class));
            case BoardUpdatedEvent.BOARD_UPDATED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), BoardUpdatedEvent.class));
            case UserInvitedEvent.USER_INVITED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class));
            case InvitationReviewedEvent.INVITATION_REVIEWED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationReviewedEvent.class));
            case InvitationDeletedEvent.INVITATION_DELETED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationDeletedEvent.class));
        }
    }

    private void handle(InvitationDeletedEvent event) {
        this.invitedIds.remove(event.getUserId());
    }

    private void handle(InvitationReviewedEvent event) {
        if (event.isAccepted()) {
            this.joinedIds.add(event.getUserId());
        }
        this.invitedIds.remove(event.getUserId());
    }

    private void handle(UserInvitedEvent event) {
        this.invitedIds.add(event.getToUserId());
    }

    private void handle(BoardUpdatedEvent event) {
        this.title = event.getTitle();
    }

    private void handle(BoardMemberDeletedEvent event) {
        this.invitedIds.remove(event.getMemberId());
    }

    private void handle(BoardCreatedEvent event) {
        this.title = event.getTitle();
        this.ownerId = event.getOwnerId();
        this.invitedIds = Collections.synchronizedSet(new HashSet<>());
        this.joinedIds = Collections.synchronizedSet(new HashSet<>());
    }

    private void handle(BoardDeletedEvent ignored) {
        this.isDeleted = true;
    }

    public void createBoard(String title, String ownerId) {
        final var data = BoardCreatedEvent.builder()
                .aggregateId(id)
                .title(title)
                .ownerId(ownerId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BoardCreatedEvent.BOARD_CREATED_V1, dataBytes);
        this.apply(event);
    }

    public void deleteBoard() {
        final var data = BoardDeletedEvent.builder()
                .aggregateId(id)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BoardDeletedEvent.BOARD_DELETED_V1, dataBytes);
        this.apply(event);
    }

    public void deleteMember(String memberId) {
        final var data = BoardMemberDeletedEvent.builder()
                .aggregateId(id)
                .memberId(memberId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BoardMemberDeletedEvent.BOARD_MEMBER_DELETED_V1, dataBytes);
        this.apply(event);
    }

    public void updateBoard(String title) {
        final var data = BoardUpdatedEvent.builder()
                .aggregateId(id)
                .title(title)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(BoardUpdatedEvent.BOARD_UPDATED_V1, dataBytes);
        this.apply(event);
    }

    public void inviteUser(String userId) {
        final var data = UserInvitedEvent.builder()
                .aggregateId(id)
                .fromUserId(ownerId)
                .toUserId(userId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(UserInvitedEvent.USER_INVITED_V1, dataBytes);
        this.apply(event);
    }

    public void reviewInvitation(String userId, boolean accepted) {
        final var data = InvitationReviewedEvent.builder()
                .aggregateId(id)
                .userId(userId)
                .accepted(accepted)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(InvitationReviewedEvent.INVITATION_REVIEWED_V1, dataBytes);
        this.apply(event);
    }

    public void deleteIntimation(String userId) {
        final var data = InvitationDeletedEvent.builder()
                .aggregateId(id)
                .userId(userId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(InvitationDeletedEvent.INVITATION_DELETED_V1, dataBytes);
        this.apply(event);
    }
}

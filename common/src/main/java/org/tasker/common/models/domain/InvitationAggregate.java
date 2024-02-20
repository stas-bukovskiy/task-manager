package org.tasker.common.models.domain;

import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.InvitationReviewedEvent;
import org.tasker.common.models.event.UserInvitedEvent;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class InvitationAggregate extends AggregateRoot {

    public static final String AGGREGATE_TYPE = "invitation_aggregate";

    private String boardId;
    private String fromUserId;
    private String toUserId;
    private Boolean accepted;


    public InvitationAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case UserInvitedEvent.USER_INVITED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class));
            case InvitationReviewedEvent.INVITATION_REVIEWED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), InvitationReviewedEvent.class));
        }
    }

    private void handle(UserInvitedEvent event) {
        this.boardId = event.getBoardId();
        this.fromUserId = event.getFromUserId();
        this.toUserId = event.getToUserId();
    }

    private void handle(InvitationReviewedEvent event) {
        this.accepted = event.isAccepted();
    }

    public void createInvitation(String boardTitle, String boardId, String fromUserName, String fromUserId, String toUserId) {
        final var data = UserInvitedEvent.builder()
                .aggregateId(id)
                .boardTitle(boardTitle)
                .boardId(boardId)
                .fromUserName(fromUserName)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(UserInvitedEvent.USER_INVITED_V1, dataBytes);
        this.apply(event);
    }

    public void reviewInvitation(String boardTitle, String toUsername, String toUserId, boolean accepted) {
        final var data = InvitationReviewedEvent.builder()
                .aggregateId(id)
                .boardTitle(boardTitle)
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .toUsername(toUsername)
                .boardId(boardId)
                .accepted(accepted)
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(InvitationReviewedEvent.INVITATION_REVIEWED_V1, dataBytes);
        this.apply(event);
    }
}

package org.tasker.common.models.domain;

import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
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
    private String toUserIds;


    public InvitationAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case UserInvitedEvent.USER_INVITED_V1 ->
                    handle(SerializerUtils.deserializeFromJsonBytes(event.getData(), UserInvitedEvent.class));
        }
    }

    private void handle(UserInvitedEvent event) {
        this.boardId = event.getBoardId();
        this.fromUserId = event.getFromUserId();
        this.toUserIds = event.getToUserId();
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
}

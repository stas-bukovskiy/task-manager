package org.tasker.common.models.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.InvitationAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvitationReviewedEvent extends BaseEvent {

    public static final String INVITATION_REVIEWED_V1 = "INVITATION_REVIEWED_V1";
    public static final String AGGREGATE_TYPE = InvitationAggregate.AGGREGATE_TYPE;

    private String boardTitle;
    private String boardId;
    private String fromUserId;
    private String toUsername;
    private String toUserId;
    private boolean accepted;

    @Builder
    public InvitationReviewedEvent(String aggregateId, String boardTitle, String boardId, String fromUserId,
                                   String toUsername, String toUserId, boolean accepted) {
        super(aggregateId);
        this.boardTitle = boardTitle;
        this.boardId = boardId;
        this.toUsername = toUsername;
        this.toUserId = toUserId;
        this.fromUserId = fromUserId;
        this.accepted = accepted;
    }
}

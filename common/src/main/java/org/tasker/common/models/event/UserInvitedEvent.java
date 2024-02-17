package org.tasker.common.models.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.InvitationAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserInvitedEvent extends BaseEvent {

    public static final String USER_INVITED_V1 = "USER_INVITED_V1";
    public static final String AGGREGATE_TYPE = InvitationAggregate.AGGREGATE_TYPE;

    private String boardTitle;
    private String boardId;
    private String fromUserName;
    private String fromUserId;
    private String toUserId;

    @Builder
    public UserInvitedEvent(String aggregateId, String boardTitle, String boardId, String fromUserName, String fromUserId, String toUserId) {
        super(aggregateId);
        this.boardTitle = boardTitle;
        this.boardId = boardId;
        this.fromUserName = fromUserName;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }
}


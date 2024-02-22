package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
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

    @JsonProperty("board_title")
    private String boardTitle;
    @JsonProperty("board_id")
    private String boardId;
    @JsonProperty("from_user_name")
    private String fromUserName;
    @JsonProperty("from_user_id")
    private String fromUserId;
    @JsonProperty("to_user_id")
    private String toUserId;

    @Builder
    public UserInvitedEvent(@JsonProperty("aggregate_id") String aggregateId, String boardTitle, String boardId,
                            String fromUserName, String fromUserId, String toUserId) {
        super(aggregateId);
        this.boardTitle = boardTitle;
        this.boardId = boardId;
        this.fromUserName = fromUserName;
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }
}


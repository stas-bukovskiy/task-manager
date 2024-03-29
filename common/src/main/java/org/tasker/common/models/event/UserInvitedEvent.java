package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.BoardAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserInvitedEvent extends BaseEvent {

    public static final String USER_INVITED_V1 = "USER_INVITED_V1";
    public static final String AGGREGATE_TYPE = BoardAggregate.AGGREGATE_TYPE;

    @JsonProperty("from_user_id")
    private String fromUserId;
    @JsonProperty("to_user_id")
    private String toUserId;

    @Builder
    public UserInvitedEvent(@JsonProperty("aggregate_id") String aggregateId, String fromUserId, String toUserId) {
        super(aggregateId);
        this.fromUserId = fromUserId;
        this.toUserId = toUserId;
    }
}


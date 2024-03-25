package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.BoardAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class InvitationReviewedEvent extends BaseEvent {

    public static final String INVITATION_REVIEWED_V1 = "INVITATION_REVIEWED_V1";
    public static final String AGGREGATE_TYPE = BoardAggregate.AGGREGATE_TYPE;

    @JsonProperty("user_id")
    private String userId;
    private boolean accepted;

    @Builder
    public InvitationReviewedEvent(@JsonProperty("aggregate_id") String aggregateId, String userId, boolean accepted) {
        super(aggregateId);
        this.userId = userId;
        this.accepted = accepted;
    }
}

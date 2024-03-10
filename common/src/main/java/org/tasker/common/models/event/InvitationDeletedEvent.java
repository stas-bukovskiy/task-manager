package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.BoardAggregate;

@Data
@EqualsAndHashCode(callSuper = true)
public class InvitationDeletedEvent extends BaseEvent {

    public static final String INVITATION_DELETED_V1 = "INVITATION_DELETED_V1";
    public static final String AGGREGATE_TYPE = BoardAggregate.AGGREGATE_TYPE;

    private String userId;

    @Builder
    public InvitationDeletedEvent(@JsonProperty("aggregate_id") String aggregateId, String userId) {
        super(aggregateId);
        this.userId = userId;
    }
}

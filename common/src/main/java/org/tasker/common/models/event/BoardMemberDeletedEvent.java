package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.BoardAggregate;


@Data
@EqualsAndHashCode(callSuper = true)
public class BoardMemberDeletedEvent extends BaseEvent {

    public static final String BOARD_MEMBER_DELETED_V1 = "BOARD_MEMBER_DELETED_V1";
    public static final String AGGREGATE_TYPE = BoardAggregate.AGGREGATE_TYPE;

    private String memberId;

    @Builder
    public BoardMemberDeletedEvent(@JsonProperty("aggregate_id") String aggregateId, String memberId) {
        super(aggregateId);
        this.memberId = memberId;
    }
}

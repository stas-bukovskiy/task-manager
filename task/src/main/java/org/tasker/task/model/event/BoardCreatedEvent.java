package org.tasker.task.model.event;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.task.model.domain.BoardAggregate;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class BoardCreatedEvent extends BaseEvent {

    public static final String BOARD_CREATED_V1 = "BOARD_CREATED_V1";
    public static final String AGGREGATE_TYPE = BoardAggregate.AGGREGATE_TYPE;

    private String title;
    private String ownerId;
    private List<String> invitedIds;

    @Builder
    public BoardCreatedEvent(String aggregateId, String title, String ownerId, List<String> invitedIds) {
        super(aggregateId);
        this.title = title;
        this.ownerId = ownerId;
        this.invitedIds = invitedIds;
    }
}


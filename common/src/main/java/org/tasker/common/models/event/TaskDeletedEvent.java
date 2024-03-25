package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.TaskAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskDeletedEvent extends BaseEvent {

    public static final String TASK_DELETED_V1 = "TASK_DELETED_V1";
    public static final String AGGREGATE_TYPE = TaskAggregate.AGGREGATE_TYPE;

    @JsonProperty("board_id")
    private String boardId;

    @Builder
    public TaskDeletedEvent(@JsonProperty("aggregate_id") String aggregateId, String boardId) {
        super(aggregateId);
        this.boardId = boardId;
    }

}

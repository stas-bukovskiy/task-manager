package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.TaskAggregate;
import org.tasker.common.models.enums.TaskStatus;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskStatusUpdatedEvent extends BaseEvent {

    public static final String TASK_STATUS_UPDATED_V1 = "TASK_STATUS_UPDATED_V1";
    public static final String AGGREGATE_TYPE = TaskAggregate.AGGREGATE_TYPE;

    private TaskStatus status;

    @Builder
    public TaskStatusUpdatedEvent(@JsonProperty("aggregate_id") String aggregateId, TaskStatus status) {
        super(aggregateId);
        this.status = status;
    }
}


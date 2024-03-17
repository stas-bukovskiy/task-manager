package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.TaskAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskAssigneeAdded extends BaseEvent {

    public static final String TASK_ASSIGNEE_ADDED_V1 = "TASK_ASSIGNEE_ADDED_V1";
    public static final String AGGREGATE_TYPE = TaskAggregate.AGGREGATE_TYPE;

    @JsonProperty("assignee_id")
    private String assigneeId;

    @Builder
    public TaskAssigneeAdded(@JsonProperty("aggregate_id") String aggregateId, String assigneeId) {
        super(aggregateId);
        this.assigneeId = assigneeId;
    }
}


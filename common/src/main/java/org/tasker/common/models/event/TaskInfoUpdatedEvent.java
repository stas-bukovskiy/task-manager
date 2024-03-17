package org.tasker.common.models.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.TaskAggregate;
import org.tasker.common.models.enums.TaskPriority;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class TaskInfoUpdatedEvent extends BaseEvent {

    public static final String TASK_INFO_UPDATED_V1 = "TASK_INFO_UPDATED_V1";
    public static final String AGGREGATE_TYPE = TaskAggregate.AGGREGATE_TYPE;

    private String title;
    private String description;

    @JsonProperty("start_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date startDate;

    @JsonProperty("due_date")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private Date dueDate;

    @JsonProperty("estimated_time")
    private int estimatedTime;
    private TaskPriority priority;

    @Builder
    public TaskInfoUpdatedEvent(@JsonProperty("aggregate_id") String aggregateId, String title, String description, Date startDate, Date dueDate, int estimatedTime, TaskPriority priority) {
        super(aggregateId);
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.estimatedTime = estimatedTime;
        this.priority = priority;
    }
}


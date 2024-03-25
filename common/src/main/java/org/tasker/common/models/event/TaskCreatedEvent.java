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
public class TaskCreatedEvent extends BaseEvent {

    public static final String TASK_CREATED_V1 = "TASK_CREATED_V1";
    public static final String AGGREGATE_TYPE = TaskAggregate.AGGREGATE_TYPE;

    @JsonProperty("board_id")
    private String boardId;
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
    @JsonProperty("created_by")
    private String createdBy;

    @Builder
    public TaskCreatedEvent(@JsonProperty("aggregate_id") String aggregateId, String boardId, String title, String description, Date startDate, Date dueDate, int estimatedTime, TaskPriority priority, String createdBy) {
        super(aggregateId);
        this.boardId = boardId;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.dueDate = dueDate;
        this.estimatedTime = estimatedTime;
        this.priority = priority;
        this.createdBy = createdBy;
    }
}


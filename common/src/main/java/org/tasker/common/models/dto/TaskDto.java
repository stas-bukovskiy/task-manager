package org.tasker.common.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.tasker.common.models.enums.TaskPriority;
import org.tasker.common.models.enums.TaskStatus;

import java.util.Set;

@Builder
public record TaskDto(
        @JsonProperty("aggregate_id")
        String aggregateId,
        String title,
        String description,
        @JsonProperty("start_date")
        String startDate,

        @JsonProperty("due_date")
        String dueDate,

        @JsonProperty("estimated_time")
        int estimatedTime,
        TaskPriority priority,
        TaskStatus status,

        @JsonProperty("assignee_ids")
        Set<String> assigneeIds,

        @JsonProperty("board_id")
        String boardId
) {
}

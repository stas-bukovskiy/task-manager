package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

public record DeleteAssigneeRequest(
        @NotNull(message = "task_id is required")
        @UUID(message = "task_id is not valid UUID format")
        @JsonProperty("task_id")
        String taskId,
        @NotNull(message = "assignee_id is required")
        @UUID(message = "assignee_id is not valid UUID format")
        @JsonProperty("assignee_id")
        String assigneeId
) {
}

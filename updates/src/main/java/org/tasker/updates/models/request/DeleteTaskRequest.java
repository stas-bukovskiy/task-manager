package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;

public record DeleteTaskRequest(
        @NotNull(message = "task_id is required")
        @UUID(message = "task_id is not valid UUID format")
        @JsonProperty("task_id")
        String taskId
) {
}

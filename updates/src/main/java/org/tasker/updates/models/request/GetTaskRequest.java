package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.UUID;

public record GetTaskRequest(
        @UUID(message = "Invalid task_id format. Should be UUID.")
        @JsonProperty("task_id")
        String taskId
) {
}

package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;
import org.tasker.common.models.enums.TaskStatus;
import org.tasker.updates.validation.ValidEnum;

public record UpdateTaskStatusRequest(

        @NotNull(message = "task_id is required")
        @UUID(message = "task_id is not valid UUID format")
        @JsonProperty("task_id")
        String taskId,

        @NotNull(message = "status is required")
        @NotBlank(message = "status is required")
        @ValidEnum(enumClass = TaskStatus.class, message = "status is not valid")
        String status
) {

}

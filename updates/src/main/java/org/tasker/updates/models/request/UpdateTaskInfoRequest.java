package org.tasker.updates.models.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.tasker.common.models.enums.TaskPriority;
import org.tasker.updates.validation.ValidEnum;

import java.util.Date;


public record UpdateTaskInfoRequest(
        @UUID(message = "task_id is not valid UUID format")
        @NotNull(message = "task_id is required")
        @JsonProperty("task_id")
        String taskId,

        @NotNull(message = "title is required")
        @NotBlank(message = "title is required")
        String title,

        String description,

        @JsonProperty("start_date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Date startDate,

        @JsonProperty("due_date")
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Date dueDate,

        @Min(value = 0, message = "estimated_time should be greater than to 0")
        @JsonProperty("estimated_time")
        int estimatedTime,

        @NotNull(message = "priority is required")
        @NotBlank(message = "priority is required")
        @ValidEnum(enumClass = TaskPriority.class, message = "priority is not valid")
        String priority

) {

}

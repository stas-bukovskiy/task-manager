package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.tasker.common.models.enums.TaskPriority;
import org.tasker.common.models.enums.TaskStatus;
import org.tasker.updates.validation.ValidEnum;

import java.util.Date;
import java.util.List;

public record CreateTaskRequest(
        @UUID(message = "board_id is not valid UUID format")
        @NotNull(message = "board_id is required")
        @JsonProperty("board_id")
        String boardId,

        @NotNull(message = "title is required")
        @NotBlank(message = "title is required")
        String title,

        String description,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @JsonProperty("start_date")
        Date startDate,

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        @JsonProperty("due_date")
        Date dueDate,

        @Min(value = 0, message = "estimated_time should be greater than to 0")
        @JsonProperty("estimated_time")
        int estimatedTime,

        @NotNull(message = "priority is required")
        @NotBlank(message = "priority is required")
        @ValidEnum(enumClass = TaskPriority.class, message = "priority is not valid")
        String priority,

        @NotNull(message = "status is required")
        @NotBlank(message = "status is required")
        @ValidEnum(enumClass = TaskStatus.class, message = "status is not valid")
        String status,

        @JsonProperty("assignee_ids")
        List<String> assigneeIds
) {

}

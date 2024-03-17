package org.tasker.common.models.commands;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import org.tasker.common.models.enums.TaskPriority;

import java.util.Date;

@Builder
public record UpdateTaskInfoCommand(
        String taskId,
        String userId,
        String title,
        String description,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Date startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Date dueDate,
        int estimatedTime,
        TaskPriority priority
) {
    public static final String COMMAND_NAME = "update_task_info";
}

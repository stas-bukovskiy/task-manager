package org.tasker.common.models.commands;

import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import org.tasker.common.models.enums.TaskPriority;
import org.tasker.common.models.enums.TaskStatus;

import java.util.Date;
import java.util.List;

@Builder
public record CreateTaskCommand(
        String boardId,
        String userId,
        String title,
        String description,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Date startDate,
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
        Date dueDate,
        int estimatedTime,
        TaskPriority priority,
        TaskStatus status,
        List<String> assigneeIds
) {
    public static final String COMMAND_NAME = "create_task";
}
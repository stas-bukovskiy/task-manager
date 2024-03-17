package org.tasker.common.models.commands;

import lombok.Builder;
import org.tasker.common.models.enums.TaskStatus;

@Builder
public record UpdateTaskStatusCommand(
        String taskId,
        String userId,
        TaskStatus status
) {
    public static final String COMMAND_NAME = "update_task_status";
}

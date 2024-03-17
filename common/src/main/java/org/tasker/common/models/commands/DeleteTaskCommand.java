package org.tasker.common.models.commands;

import lombok.Builder;

@Builder
public record DeleteTaskCommand(
        String taskId,
        String userId
) {
    public static final String COMMAND_NAME = "delete_task";
}
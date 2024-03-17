package org.tasker.common.models.commands;

import lombok.Builder;

@Builder
public record DeleteAssigneeCommand(
        String taskId,
        String userId,
        String assigneeId
) {
    public static final String COMMAND_NAME = "delete_assignee";
}
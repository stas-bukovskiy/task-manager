package org.tasker.common.models.commands;

import lombok.Builder;

@Builder
public record AddAssigneeCommand(
        String taskId,
        String userId,
        String assigneeId
) {
    public static final String COMMAND_NAME = "add_assignee";
}

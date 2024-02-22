package org.tasker.common.models.commands;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateBoardCommand(
        String title,
        String ownerId,
        List<String> invitedUserIds
) {
    public static final String COMMAND_NAME = "create_board";
}

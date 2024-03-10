package org.tasker.common.models.commands;

import lombok.Builder;

@Builder
public record DeleteBoardCommand(
        String boardId,
        String userId
) {
    public static final String COMMAND_NAME = "delete_board";
}

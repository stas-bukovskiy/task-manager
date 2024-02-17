package org.tasker.common.models.commands;

import lombok.Builder;

import java.util.List;

@Builder
public record CreateBoardCommand(
        String title,
        String ownerId,
        List<String> invitedPeopleIds
) {
    public static final String COMMAND_NAME = "create_board";
}

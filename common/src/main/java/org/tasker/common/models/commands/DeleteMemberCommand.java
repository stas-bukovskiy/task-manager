package org.tasker.common.models.commands;

import lombok.Builder;

@Builder
public record DeleteMemberCommand(
        String boardId,
        String userId,
        String memberId
) {
    public static final String COMMAND_NAME = "delete_member";
}

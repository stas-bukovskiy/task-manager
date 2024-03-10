package org.tasker.common.models.commands;

import lombok.Builder;

@Builder
public record ReviewInvitationCommand(
        String boardId,
        String userId,
        boolean isAccepted
) {
    public static final String COMMAND_NAME = "review_invitation";
}

package org.tasker.common.models.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record DeleteInvitationCommand(
        @JsonProperty("board_id")
        String boardId,
        @JsonProperty("owner_id")
        String ownerId,
        @JsonProperty("user_id")
        String userId
) {
    public static final String COMMAND_NAME = "delete_invitation";
}

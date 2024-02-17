package org.tasker.common.models.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record InviteUsersCommand(
        @JsonProperty("board_id")
        String boardId,

        @JsonProperty("from_user_id")
        String fromUserId,

        @JsonProperty("to_user_ids")
        List<String> toUserIds
) {
    public static final String COMMAND_NAME = "invite_users_to_board";
}

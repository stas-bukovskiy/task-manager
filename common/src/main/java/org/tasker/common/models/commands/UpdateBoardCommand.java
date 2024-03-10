package org.tasker.common.models.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UpdateBoardCommand(
        @JsonProperty("board_id")
        String boardId,
        String title,
        @JsonProperty("user_id")
        String userId
) {
    public static final String COMMAND_NAME = "update_board";
}

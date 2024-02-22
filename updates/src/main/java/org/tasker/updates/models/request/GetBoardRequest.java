package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.UUID;

public record GetBoardRequest(
        @UUID(message = "Invalid board_id")
        @JsonProperty("board_id")
        String boardId
) {
}

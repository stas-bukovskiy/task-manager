package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.hibernate.validator.constraints.UUID;

@Builder
public record DeleteBoardRequest(
        @UUID(message = "board_id should be a valid UUID")
        @JsonProperty("board_id")
        String boardId
) {
}

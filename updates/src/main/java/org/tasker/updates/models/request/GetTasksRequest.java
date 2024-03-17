package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.UUID;

public record GetTasksRequest(
        @UUID(message = "Invalid board_id format. Should be UUID.")
        @JsonProperty("board_id")
        String boardId
) {
}

package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.UUID;

public record DeleteInvitationRequest(
        @UUID(message = "board_id should be a valid UUID")
        @JsonProperty("board_id")
        String boardId,

        @UUID(message = "user_id should be a valid UUID")
        @JsonProperty("user_id")
        String userId
) {
}

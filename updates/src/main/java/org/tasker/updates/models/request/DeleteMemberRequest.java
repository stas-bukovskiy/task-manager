package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import org.hibernate.validator.constraints.UUID;

@Builder
public record DeleteMemberRequest(
        @UUID(message = "board_id should be a valid UUID")
        @JsonProperty("board_id")
        String boardId,
        @UUID(message = "user_id should be a valid UUID")
        @JsonProperty("user_id")
        String userId
) {
}

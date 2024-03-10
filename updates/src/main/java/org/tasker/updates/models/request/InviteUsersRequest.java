package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.UUID;

import java.util.List;

public record InviteUsersRequest(
        @UUID(message = "board_id should be a valid UUID")
        @JsonProperty("board_id")
        String boardId,

        @Size(min = 1, message = "to_user_ids should contain at least one user id")
        @JsonProperty("to_user_ids")
        List<String> toUserIds
) {
}

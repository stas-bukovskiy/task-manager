package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import org.hibernate.validator.constraints.UUID;

@Builder
public record UpdateBoardRequest(
        @UUID(message = "board_id should be a valid UUID")
        @NotNull(message = "board_id cannot be null")
        @JsonProperty("board_id")
        String boardId,
        @NotNull(message = "title cannot be null")
        @Size(min = 3, max = 20, message = "title must be at least 3 characters and less than 20 characters")
        String title
) {
}

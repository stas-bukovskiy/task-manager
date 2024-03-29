package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateBoardRequest(
        @NotNull(message = "title cannot be null")
        @Size(min = 3, max = 20, message = "title must be at least 3 characters and less than 20 characters")
        String title,
        @JsonProperty("invited_user_ids")
        List<String> invitedUserIds
) {

}

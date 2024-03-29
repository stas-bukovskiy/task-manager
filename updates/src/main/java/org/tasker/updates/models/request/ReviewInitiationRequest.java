package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ReviewInitiationRequest(
        @JsonProperty("board_id")
        String boardId,
        @JsonProperty("is_accepted")
        boolean isAccepted
) {

}

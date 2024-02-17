package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record CreateBoardRequest(
        String title,
        @JsonProperty("invited_user_ids")
        List<String> invitedUserIds
) {

}

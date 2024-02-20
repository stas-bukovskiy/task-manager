package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ReviewInitiationRequest(
        @JsonProperty("invitation_id")
        String invitationId,
        @JsonProperty("is_accepted")
        boolean isAccepted
) {

}

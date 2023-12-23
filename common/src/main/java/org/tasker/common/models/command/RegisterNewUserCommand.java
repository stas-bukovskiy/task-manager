package org.tasker.common.models.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record RegisterNewUserCommand(
        @JsonProperty("aggregate_id")
        String aggregateID,
        String username,
        String email,
        String password,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName
) {
}

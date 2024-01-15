package org.tasker.common.models.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UpdateUserCommand(
        @JsonProperty("aggregate_id")
        String aggregateID,

        String username,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName
) {
    public static final String COMMAND_NAME = "update_user_info";
}

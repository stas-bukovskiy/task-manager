package org.tasker.common.models.commands;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record RegisterNewUserCommand(
        String username,
        String email,
        String password,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName
) {
    public static final String COMMAND_NAME = "register";
}

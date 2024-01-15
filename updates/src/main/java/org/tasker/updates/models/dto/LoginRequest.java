package org.tasker.updates.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LoginRequest(
        @NotNull(message = "login cannot be null")
        @Size(min = 3, max = 20, message = "login must be between 3 and 20 characters")
        String login,

        @NotNull(message = "password cannot be null")
        @Size(min = 6, max = 20, message = "password must be between 6 and 20 characters")
        String password
) {
}

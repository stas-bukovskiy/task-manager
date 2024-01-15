package org.tasker.updates.models.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VerifyTokenRequest(
        @NotNull(message = "token cannot be null")
        @Pattern(regexp = "^[\\w-]+\\.[\\w-]+\\.[\\w-]+$", message = "token is not valid")
        String token
) {
}

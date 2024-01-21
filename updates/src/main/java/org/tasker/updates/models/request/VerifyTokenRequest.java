package org.tasker.updates.models.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;

@Builder
public record VerifyTokenRequest(
        @NotNull(message = "token cannot be null")
        @Pattern(regexp = "^[\\w-]+\\.[\\w-]+\\.[\\w-]+$", message = "token is not valid")
        String token
) {
}

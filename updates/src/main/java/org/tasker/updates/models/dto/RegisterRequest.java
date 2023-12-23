package org.tasker.updates.models.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.*;

public record RegisterRequest(
        @NotNull(message = "username cannot be null")
        @Min(value = 3, message = "username must be at least 3 characters")
        @Max(value = 20, message = "username must be less than 20 characters")
        String username,
        @NotNull(message = "email cannot be null")
        @Email(message = "email should be valid")
        String email,

        @NotNull(message = "password cannot be null")
        @Min(value = 6, message = "password must be at least 8 characters")
        @Max(value = 20, message = "password must be less than 20")
        String password,


        @NotNull(message = "first_name cannot be null")
        @NotBlank(message = "first_name cannot be blank")
        @JsonProperty("first_name")
        String firstName,

        @Nullable()
        @JsonProperty("last_name")
        String lastName
) {
}

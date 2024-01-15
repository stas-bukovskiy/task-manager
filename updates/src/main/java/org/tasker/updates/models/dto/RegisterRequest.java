package org.tasker.updates.models.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotNull(message = "username cannot be null")
        @Size(min = 3, max = 20, message = "username must be at least 3 characters and less than 20 characters")
        String username,
        @NotNull(message = "email cannot be null")
        @Email(message = "email should be valid")
        String email,

        @NotNull(message = "password cannot be null")
        @Size(min = 6, max = 20, message = "username must be at least 6 characters and less than 20 characters")
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

package org.tasker.common.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UserDto(
        String id,

        String username,
        String email,
        @JsonProperty("first_name")
        String firstName,
        @JsonProperty("last_name")
        String lastName
) {
}

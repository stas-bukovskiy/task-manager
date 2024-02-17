package org.tasker.updates.models.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

@Builder
public record SearchPeopleRequest(
        @NotNull(message = "search string cannot be null")
        @Size(min = 3, max = 20, message = "search string should be from 3 to 20 chars")
        String search
) {
}

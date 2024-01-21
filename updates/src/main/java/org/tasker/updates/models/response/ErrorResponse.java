package org.tasker.updates.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record ErrorResponse(
        int status,
        String message,
        long timestamp,
        @JsonProperty("correlation_id")
        String correlationId
) {
}

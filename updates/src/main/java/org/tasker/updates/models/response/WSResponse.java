package org.tasker.updates.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record WSResponse<T>(
        @JsonProperty("correlation_id")
        String correlationId,
        T data
) {
}

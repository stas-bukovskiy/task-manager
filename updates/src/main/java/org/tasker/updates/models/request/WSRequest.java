package org.tasker.updates.models.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record WSRequest(
        @JsonProperty("correlation_id")
        String correlationId,
        String type,
        byte[] data
) {
}

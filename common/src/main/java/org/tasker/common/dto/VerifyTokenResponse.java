package org.tasker.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record VerifyTokenResponse(
        @JsonProperty("aggregate_id")
        String aggregateID,

        @JsonProperty("http_code")
        int httpCode,

        String message
) {
}

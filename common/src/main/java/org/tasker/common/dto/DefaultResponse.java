package org.tasker.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record DefaultResponse(
        @JsonProperty("http_code")
        int httpCode,

        String message

) {
}

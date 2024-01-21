package org.tasker.common.models.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class Response<T> {
    @JsonProperty("http_code")
    private final int httpCode;
    private final String message;
    private final T data;
}

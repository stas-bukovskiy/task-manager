package org.tasker.updates.models.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record UpdateResponse<T>(
        @JsonIgnore
        String toUserId,
        @JsonProperty("update_type")
        UpdateType updateType,
        String type,
        String message,
        T data
) {
    public enum UpdateType {
        NOTIFICATION,
        UPDATE
    }
}

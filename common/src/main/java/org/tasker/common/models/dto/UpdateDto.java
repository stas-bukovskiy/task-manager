package org.tasker.common.models.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

import java.util.List;

@Builder
public record UpdateDto<T>(

        @JsonProperty("to_user_ids")
        List<String> toUserIds,
        String type,
        String message,
        T data
) {
    public enum UpdateType {
        NOTIFICATION,
        UPDATE
    }
}

package org.tasker.updates.models.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

@Builder
public record NotificationResponse<T>(
        @JsonIgnore
        String toUserId,
        String type,
        String message,
        T data
) {
}

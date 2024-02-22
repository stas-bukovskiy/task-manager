package org.tasker.common.models.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BoardDto(
        String id,
        String title,
        String ownerId,
        List<String> invitedIds,
        List<String> joinedIds
) {
}

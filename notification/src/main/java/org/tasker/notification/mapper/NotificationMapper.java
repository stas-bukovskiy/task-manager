package org.tasker.notification.mapper;

import org.tasker.common.models.domain.NotificationAggregate;
import org.tasker.notification.models.domain.NotificationDocument;

public final class NotificationMapper {

    private NotificationMapper() {
    }

    public static NotificationDocument fromAggToDoc(NotificationAggregate agg) {
        return NotificationDocument.builder()
                .aggregateId(agg.getId())
                .userId(agg.getUserId())
                .forAggregateId(agg.getForAggregateId())
                .message(agg.getMessage())
                .forAggregateType(agg.getForAggregateType())
                .valid(agg.isValid())
                .deleted(agg.isDeleted())
                .createdAt(agg.getCreatedAt())
                .build();
    }

}

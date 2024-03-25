package org.tasker.common.models.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.tasker.common.es.AggregateRoot;
import org.tasker.common.es.Event;
import org.tasker.common.es.SerializerUtils;
import org.tasker.common.models.event.NotificationCreatedEvent;
import org.tasker.common.models.event.NotificationInvalidatedEvent;

import java.util.Date;

@Getter
@Setter
@ToString
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class NotificationAggregate extends AggregateRoot {

    public static final String AGGREGATE_TYPE = "notification_aggregate";

    @JsonProperty("user_id")
    private String userId;
    private String message;
    @JsonProperty("for_aggregate_type")
    private String forAggregateType;
    @JsonProperty("for_aggregate_id")
    private String forAggregateId;
    @JsonProperty("is_valid")
    private boolean isValid;
    @JsonProperty("is_deleted")
    private boolean isDeleted;
    @JsonProperty("created_at")
    private Date createdAt;


    public NotificationAggregate(String id) {
        super(id, AGGREGATE_TYPE);
    }

    @Override
    public void when(Event event) {
        switch (event.getEventType()) {
            case NotificationCreatedEvent.NOTIFICATION_CREATED_V1 -> {
                final var notificationCreatedEvent = SerializerUtils.deserializeFromJsonBytes(event.getData(), NotificationCreatedEvent.class);
                handle(notificationCreatedEvent);
            }
            case NotificationInvalidatedEvent.NOTIFICATION_INVALIDATED_V1 -> {
                this.isValid = false;
            }
        }
    }

    private void handle(NotificationCreatedEvent event) {
        this.userId = event.getUserId();
        this.message = event.getMessage();
        this.forAggregateType = event.getForAggregateType();
        this.forAggregateId = event.getForAggregateId();
        this.isValid = true;
        this.isDeleted = false;
        this.createdAt = event.getCreatedAt();
    }

    public void createNotification(String userId, String forAggregateId, String message, String forAggregateType) {
        final var data = NotificationCreatedEvent.builder()
                .aggregateId(this.getId())
                .userId(userId)
                .message(message)
                .forAggregateType(forAggregateType)
                .forAggregateId(forAggregateId)
                .createdAt(new Date())
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(NotificationCreatedEvent.NOTIFICATION_CREATED_V1, dataBytes);
        this.apply(event);
    }


    public void invalidate() {
        final var data = NotificationInvalidatedEvent.builder()
                .aggregateId(this.getId())
                .build();

        final byte[] dataBytes = SerializerUtils.serializeToJsonBytes(data);
        final var event = this.createEvent(NotificationInvalidatedEvent.NOTIFICATION_INVALIDATED_V1, dataBytes);
        this.apply(event);
    }
}

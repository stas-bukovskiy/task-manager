package org.tasker.common.models.event;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.tasker.common.es.BaseEvent;
import org.tasker.common.models.domain.NotificationAggregate;

@Data
@EqualsAndHashCode(callSuper = false)
public class NotificationInvalidatedEvent extends BaseEvent {

    public static final String NOTIFICATION_INVALIDATED_V1 = "NOTIFICATION_INVALIDATED_V1";
    public static final String AGGREGATE_TYPE = NotificationAggregate.AGGREGATE_TYPE;

    @Builder
    public NotificationInvalidatedEvent(@JsonProperty("aggregate_id") String aggregateId) {
        super(aggregateId);
    }
}

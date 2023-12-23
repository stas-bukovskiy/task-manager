package org.tasker.common.es;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    private UUID id;
    private String aggregateId;
    private String eventType;
    private String aggregateType;
    private long version;
    private byte[] data;
    private LocalDateTime createdAt;

    public Event(String eventType, String aggregateType) {
        this.id = UUID.randomUUID();
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.createdAt = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Event event)) return false;

        if (version != event.version) return false;
        if (!id.equals(event.id)) return false;
        if (!aggregateId.equals(event.aggregateId)) return false;
        if (!eventType.equals(event.eventType)) return false;
        if (!aggregateType.equals(event.aggregateType)) return false;
        if (!Arrays.equals(data, event.data)) return false;
        return createdAt.truncatedTo(ChronoUnit.SECONDS)
                .isEqual(event.getCreatedAt().truncatedTo(ChronoUnit.SECONDS));
    }

    @Override
    public int hashCode() {
        int result = id.hashCode();
        result = 31 * result + aggregateId.hashCode();
        result = 31 * result + eventType.hashCode();
        result = 31 * result + aggregateType.hashCode();
        result = 31 * result + (int) (version ^ (version >>> 32));
        result = 31 * result + Arrays.hashCode(data);
        result = 31 * result + createdAt.hashCode();
        return result;
    }
}
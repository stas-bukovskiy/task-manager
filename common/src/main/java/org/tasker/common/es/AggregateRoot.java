package org.tasker.common.es;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.tasker.common.exceptions.InvalidEventException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Base class for all domain classes that are event sourced.
 */
@Data
@Getter
@NoArgsConstructor
public abstract class AggregateRoot {

    protected final List<Event> changes = new ArrayList<>();
    protected String id;
    protected String type;
    protected long version;

    public AggregateRoot(final String id, final String aggregateType) {
        this.id = id;
        this.type = aggregateType;
    }


    public abstract void when(final Event event);

    public void load(final List<Event> events) {
        events.forEach(event -> {
            this.validateEvent(event);
            this.raiseEvent(event);
            this.version++;
        });
    }

    public void apply(final Event event) {
        this.validateEvent(event);
        event.setAggregateType(this.type);

        when(event);
        changes.add(event);

        this.version++;
        event.setVersion(this.version);
    }

    public void raiseEvent(final Event event) {
        this.validateEvent(event);

        event.setAggregateType(this.type);
        when(event);

        this.version++;
    }

    public void clearChanges() {
        this.changes.clear();
    }

    public void toSnapshot() {
        this.clearChanges();
    }

    private void validateEvent(final Event event) {
        if (Objects.isNull(event) || !event.getAggregateId().equals(this.id))
            throw new InvalidEventException(event.toString());
    }

    protected Event createEvent(String eventType, byte[] data) {
        return Event.builder()
                .aggregateId(this.getId())
                .version(this.getVersion())
                .aggregateType(this.getType())
                .eventType(eventType)
                .data(Objects.isNull(data) ? new byte[]{} : data)
                .createdAt(LocalDateTime.now())
                .build();
    }

    public long getVersion() {
        return version;
    }
}
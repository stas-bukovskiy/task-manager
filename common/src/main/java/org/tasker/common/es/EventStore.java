package org.tasker.common.es;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RequiredArgsConstructor
@Slf4j
@Repository
public class EventStore implements EventStoreDB {

    private final DatabaseClient databaseClient;
    private final EventBus eventBus;

    @Value("${es.snapshot.frequency}")
    private long snapshotFrequency;


    @Override
    @Transactional
    public <T extends AggregateRoot> Mono<Void> save(T aggregate) {
        final List<Event> aggregateEvents = new ArrayList<>(aggregate.getChanges());

        return Mono.fromSupplier(() -> {
                    if (aggregate.getVersion() > 1) {
                        return this.handleConcurrency(aggregate.getId());
                    }
                    return Mono.empty();
                })
                .then(this.saveEvents(aggregate.getChanges()))
                .then(Mono.defer(() -> {
                    if (aggregate.getVersion() % snapshotFrequency == 0) {
                        return this.saveSnapshot(aggregate);
                    }
                    return Mono.empty();
                })).then(eventBus.publish(aggregateEvents));
    }

    @Override
    @Transactional(readOnly = true)
    public <T extends AggregateRoot> Mono<T> load(String aggregateId, Class<T> aggregateType) {
        return loadSnapshot(aggregateId)
                .switchIfEmpty(Mono.just(EventSourcingUtils.snapshotFromAggregate(getAggregate(aggregateId, aggregateType))))
                .map(snapshot -> EventSourcingUtils.aggregateromSnapshot(snapshot, aggregateType))
                .flatMap(aggregate -> loadEvents(aggregateId, aggregate.getVersion())
                        .collectList()
                        .doOnNext(events -> events.forEach(aggregate::raiseEvent))
                        .thenReturn(aggregate));
    }

    @Override
    public Mono<Void> saveEvents(List<Event> events) {
        return Flux.fromIterable(events)
                .flatMap(event -> {
                    event.setId(UUID.randomUUID());
                    return databaseClient.sql("INSERT INTO  events (event_id, aggregate_id, event_type, aggregate_type, version, data, created_at) VALUES (:event_id, :aggregate_id, :event_type, :aggregate_type, :version, :data, :created_at)")
                            .bind("event_id", event.getId())
                            .bind("aggregate_id", event.getAggregateId())
                            .bind("event_type", event.getEventType())
                            .bind("aggregate_type", event.getAggregateType())
                            .bind("version", event.getVersion())
                            .bind("data", event.getData())
                            .bind("created_at", event.getCreatedAt())
                            .fetch()
                            .rowsUpdated();
                })
                .reduce(0L, Long::sum)
                .doOnError(throwable -> log.error("(saveEvents) error saving events", throwable))
                .doOnSuccess(result -> log.debug("(saveEvents) saved events: {}", result))
                .then();
    }

    @Override
    public Flux<Event> loadEvents(String aggregateId, long version) {
        return databaseClient.sql("SELECT event_id ,aggregate_id, aggregate_type, event_type, data, metadata, version, created_at FROM events e WHERE e.aggregate_id = :aggregate_id AND e.version > :version ORDER BY e.version")
                .bind("aggregate_id", aggregateId)
                .bind("version", version)
                .map(row -> Event.builder()
                        .id(row.get("event_id", UUID.class))
                        .aggregateId(row.get("aggregate_id", String.class))
                        .aggregateType(row.get("aggregate_type", String.class))
                        .eventType(row.get("event_type", String.class))
                        .data(row.get("data", byte[].class))
                        .version(row.get("version", Long.class))
                        .createdAt(row.get("created_at", LocalDateTime.class))
                        .build())
                .all();
    }

    private <T extends AggregateRoot> Mono<Void> saveSnapshot(T aggregate) {
        aggregate.toSnapshot();
        final var snapshot = EventSourcingUtils.snapshotFromAggregate(aggregate);

        return databaseClient.sql("INSERT INTO snapshots (aggregate_id, aggregate_type, data, metadata, version, created_at) VALUES (:aggregate_id, :aggregate_type, :data, :version, now()) ON CONFLICT (aggregate_id) DO UPDATE SET data = :data, version = :version, created_at = now()")
                .bind("aggregate_id", snapshot.getAggregateId())
                .bind("aggregate_type", snapshot.getAggregateType())
                .bind("data", Objects.isNull(snapshot.getData()) ? new byte[]{} : snapshot.getData())
                .bind("version", snapshot.getVersion())
                .fetch()
                .rowsUpdated()
                .doOnError(throwable -> log.error("(saveSnapshot) error saving snapshot <{}>", snapshot, throwable))
                .doOnSubscribe(result -> log.debug("(saveSnapshot) saved snapshot <{}> with row updated: {}", snapshot, result))
                .then();
    }


    private Mono<Void> handleConcurrency(String aggregateId) {
        return databaseClient.sql("SELECT aggregate_id FROM events e WHERE e.aggregate_id = :aggregate_id LIMIT 1 FOR UPDATE")
                .bind("aggregate_id", aggregateId)
                .map(row -> row.get("aggregate_id", String.class))
                .first()
                .doOnError(throwable -> log.error("(handleConcurrency) error handling concurrency with aggregate ID <{}>", aggregateId, throwable))
                .doOnSubscribe(result -> log.debug("(handleConcurrency) successfully handled concurrency with aggregate ID <{}>: {}", aggregateId, result))
                .then();
    }

    private Mono<Snapshot> loadSnapshot(String aggregateId) {
        return databaseClient.sql("SELECT aggregate_id, aggregate_type, data, metadata, version, created_at FROM snapshots s WHERE s.aggregate_id = :aggregate_id")
                .bind("aggregate_id", aggregateId)
                .map(row -> Snapshot.builder()
                        .aggregateId(row.get("aggregate_id", String.class))
                        .aggregateType(row.get("aggregate_type", String.class))
                        .data(row.get("data", byte[].class))
                        .metaData(row.get("metadata", byte[].class))
                        .version(row.get("version", Long.class) == null ? 0 : row.get("version", Long.class))
                        .timeStamp(row.get("created_at", LocalDateTime.class))
                        .build())
                .first()
                .doOnError(throwable -> log.error("(loadSnapshot) error loading snapshot with aggregate ID <{}>", aggregateId, throwable))
                .doOnSubscribe(result -> log.debug("(loadSnapshot) successfully loaded snapshot with aggregate ID <{}>: {}", aggregateId, result));
    }

    private <T extends AggregateRoot> T getAggregate(final String aggregateId, final Class<T> aggregateType) {
        try {
            return aggregateType.getConstructor(String.class).newInstance(aggregateId);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


    @Override
    public Mono<Boolean> exists(String aggregateId) {
        return databaseClient.sql("SELECT count(*) > 0 FROM events WHERE aggregate_id = :aggregate_id")
                .bind("aggregate_id", aggregateId)
                .map(row -> row.get(0, Boolean.class))
                .first()
                .defaultIfEmpty(false)
                .doOnError(throwable -> log.error("(exists) error checking on existence with aggregate ID <{}>", aggregateId, throwable))
                .doOnSubscribe(result -> log.debug("(exists) successfully check on existence with aggregate ID <{}>: {}", aggregateId, result));
    }

}
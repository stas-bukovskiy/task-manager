package org.tasker.common.es;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.tasker.common.config.InfrastructureConfiguration;
import org.tasker.common.config.TestDatabaseConfiguration;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = InfrastructureConfiguration.class)
@Testcontainers
@Import(TestDatabaseConfiguration.class)
@ActiveProfiles(profiles = "test")
class EventStoreTest {

    static List<Event> events;
    @MockBean
    EventBus eventBus;
    @Autowired
    EventStore eventStore;
    @Autowired
    DatabaseClient databaseClient;

    @BeforeAll
    static void setUp() {
        Event initialCreationEvent = Event.builder()
                .id(UUID.randomUUID())
                .aggregateId("123")
                .eventType("Created")
                .aggregateType("Order")
                .version(1L)
                .data("Initial order creation data".getBytes())
                .createdAt(LocalDateTime.now())
                .build();
        Event updatedEventDataEvent = Event.builder()
                .id(UUID.randomUUID())
                .aggregateId("123")
                .eventType("UpdatedData")
                .aggregateType("Order")
                .version(2L)
                .data("Updated order data".getBytes())
                .createdAt(LocalDateTime.now())
                .build();
        Event orderApprovedEvent = Event.builder()
                .id(UUID.randomUUID())
                .aggregateId("123")
                .eventType("Approved")
                .aggregateType("Order")
                .version(3L)
                .data("Order approved data".getBytes())
                .createdAt(LocalDateTime.now())
                .build();
        Event orderShippedEvent = Event.builder()
                .id(UUID.randomUUID())
                .aggregateId("123")
                .eventType("Shipped")
                .aggregateType("Order")
                .version(4L)
                .data("Order shipped data".getBytes())
                .createdAt(LocalDateTime.now())
                .build();
        Event orderDeliveredEvent = Event.builder()
                .id(UUID.randomUUID())
                .aggregateId("123")
                .eventType("Delivered")
                .aggregateType("Order")
                .version(5L)
                .data("Order delivered data".getBytes())
                .createdAt(LocalDateTime.now())
                .build();

        events = List.of(
                initialCreationEvent,
                updatedEventDataEvent,
                orderApprovedEvent,
                orderShippedEvent,
                orderDeliveredEvent
        );

    }

    @AfterEach
    void tearDown() {
        databaseClient.sql("TRUNCATE TABLE events").then().block();
    }

    @Test
    void testSaveEvents() {
        Mono<Void> saveEvents = eventStore.saveEvents(events);

        Flux<Event> all = databaseClient.sql("SELECT * FROM events")
                .map(row -> Event.builder()
                        .id(row.get("event_id", UUID.class))
                        .aggregateId(row.get("aggregate_id", String.class))
                        .aggregateType(row.get("aggregate_type", String.class))
                        .eventType(row.get("event_type", String.class))
                        .data(row.get("data", byte[].class))
                        .version(row.get("version", Long.class) == null ? 0L : row.get("version", Long.class))
                        .createdAt(row.get("created_at", LocalDateTime.class))
                        .build())
                .all();


        StepVerifier.create(saveEvents.thenMany(all).collectList())
                .consumeNextWith(eventList -> assertThat(eventList).containsExactlyElementsOf(events))
                .verifyComplete();
    }

    @Test
    void testLoadEventsFromVersion0() {
        Mono<Void> saveEvents = Flux.fromIterable(events)
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
                .then();

        Flux<Event> loadEvents = eventStore.loadEvents(events.get(0).getAggregateId(), 0L);

        StepVerifier.create(saveEvents.thenMany(loadEvents).collectList())
                .consumeNextWith(eventList -> assertThat(eventList).containsAll(events))
                .verifyComplete();
    }


    @Test
    void testLoadEventsFromVersion4() {
        Mono<Void> saveEvents = Flux.fromIterable(events)
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
                .then();

        Flux<Event> loadEvents = eventStore.loadEvents(events.get(0).getAggregateId(), 4L);

        StepVerifier.create(saveEvents.thenMany(loadEvents).collectList())
                .consumeNextWith(eventList -> {
                    assertThat(eventList).containsExactlyElementsOf(events.subList(4, events.size()));
                })
                .verifyComplete();
    }

}
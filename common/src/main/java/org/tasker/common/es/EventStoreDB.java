package org.tasker.common.es;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface EventStoreDB {

    Mono<Void> saveEvents(final List<Event> events);

    Flux<Event> loadEvents(final String aggregateId, long version);

    <T extends AggregateRoot> Mono<Void> save(final T aggregate);

    <T extends AggregateRoot> Mono<T> load(final String aggregateId, final Class<T> aggregateType);

    Mono<Boolean> exists(final String aggregateId);

    Mono<Boolean> exists(final String aggregateId, final String aggregateType);
}

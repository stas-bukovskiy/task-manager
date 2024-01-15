package org.tasker.common.es;

import reactor.core.publisher.Mono;

import java.util.List;

public interface EventBus {
    Mono<Void> publish(List<Event> events);
}
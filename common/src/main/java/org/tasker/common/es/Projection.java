package org.tasker.common.es;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import reactor.core.publisher.Mono;

public interface Projection {

    @EventListener(ApplicationReadyEvent.class)
    default void eventListener() {
        this.declareQueue()
                .subscribe(this::subscribeToQueue);
    }

    void subscribeToQueue(String queueName);

    Mono<String> declareQueue();

}
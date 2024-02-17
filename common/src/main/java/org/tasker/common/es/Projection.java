package org.tasker.common.es;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.tasker.common.output.event.EventsMessagingSpecs;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.Receiver;

import java.util.Map;
import java.util.function.Function;

@Slf4j
public abstract class Projection {
    private final Map<String, Function<Event, Mono<Void>>> eventHandlers;
    private final Receiver receiver;
    private final String aggregateType;

    protected Projection(Receiver receiver, EventsMessagingSpecs messagingSpecs,
                         Map<String, Function<Event, Mono<Void>>> eventHandlers,
                         String aggregateType, String[] routingKeys) {
        this.eventHandlers = eventHandlers;
        this.receiver = receiver;
        this.aggregateType = aggregateType;

        messagingSpecs.declareBoundQueue(aggregateType, routingKeys).subscribe();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void subscribeToQueue() {
        receiver.consumeAutoAck(aggregateType)
                .subscribe(delivery -> {
                    log.info("Receive event to queue {}, with exchange {}, with key {}", aggregateType,
                            delivery.getEnvelope().getExchange(), delivery.getEnvelope().getRoutingKey());

                    final Event event = SerializerUtils.deserializeFromJsonBytes(delivery.getBody(), Event.class);
                    processEvent(event).subscribe();
                });
    }

    private Mono<Void> processEvent(Event event) {
        if (!eventHandlers.containsKey(event.getEventType())) {
            log.warn("Unknown event type: {}, for queue: {}", event, aggregateType);
            return Mono.empty();
        }

        final var eventHandler = eventHandlers.get(event.getEventType());
        return eventHandler.apply(event)
                .onErrorResume(err -> {
                    log.error("error occurred while processed board event {} for aggregateId {}", event.getEventType(), event.getAggregateId(), err);
                    return handleError(err, event);
                })
                .doOnSuccess(v -> log.info("event {} for aggregateId {} processed successfully", event.getEventType(), event.getAggregateId()));
    }

    protected abstract Mono<Void> handleError(Throwable err, Event event);
}

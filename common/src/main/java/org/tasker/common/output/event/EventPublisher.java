package org.tasker.common.output.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.tasker.common.es.Event;
import org.tasker.common.es.EventBus;
import org.tasker.common.es.SerializerUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.Sender;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisher implements EventBus {

    private final Sender sender;
    private final EventsMessagingSpecs messagingSpecs;


    @Override
    public Mono<Void> publish(List<Event> events) {
        return Flux.fromIterable(events)
                .map(event -> Mono.just(new OutboundMessage(messagingSpecs.getEventStoreExchange(),
                        messagingSpecs.toRoutingKey(event.getAggregateType(), event.getEventType()),
                        SerializerUtils.serializeToJsonBytes(event))))
                .flatMap(sender::send)
                .collectList()
                .doOnSuccess(results -> log.info("Events published: {}", events))
                .doOnError(ex -> log.error("Error while publishing events: {}", ex.getMessage()))
                .then();
    }
}

package org.tasker.common.output.event;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

import java.util.List;

@Getter
@Service
public final class EventsMessagingSpecs {

    private final String eventStoreExchange;
    private final Sender sender;

    public EventsMessagingSpecs(@Value("${communication.event-store.exchange}") String eventStoreExchange, Sender sender) {
        this.eventStoreExchange = eventStoreExchange;
        this.sender = sender;

        sender.declareExchange(genEventExchangeSpec())
                .subscribe();
    }

    public Mono<Void> declareBoundQueue(String aggregateType, String... routingKeys) {
        return sender.declareQueue(genEventQueueSpecs(aggregateType))
                .thenMany(Flux.fromArray(routingKeys)
                        .flatMap(routingKey -> sender.bind(BindingSpecification.binding(eventStoreExchange, routingKey, aggregateType))))
                .then();
    }

    public ExchangeSpecification genEventExchangeSpec() {
        return ExchangeSpecification.exchange(eventStoreExchange)
                .type("topic")
                .durable(true);
    }

    public QueueSpecification genEventQueueSpecs(String aggregateType) {
        return QueueSpecification.queue(aggregateType)
                .durable(true);
    }

    public List<BindingSpecification> genEvenBindSpecs(String queueNameToBind, List<String> routingKeys) {
        if (routingKeys.isEmpty())
            return List.of();

        return routingKeys.stream()
                .map(routingKey -> BindingSpecification.binding(eventStoreExchange, routingKey, queueNameToBind))
                .toList();
    }

    public String toRoutingKey(String aggregateType, String eventType) {
        return aggregateType + "." + eventType;
    }

}

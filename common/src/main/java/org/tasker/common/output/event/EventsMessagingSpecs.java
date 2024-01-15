package org.tasker.common.output.event;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.QueueSpecification;

import java.util.List;

@Getter
@Service
public final class EventsMessagingSpecs {

    private final String eventStoreExchange;

    public EventsMessagingSpecs(@Value("${communication.event-store.exchange}") String eventStoreExchange) {
        this.eventStoreExchange = eventStoreExchange;
    }

    public ExchangeSpecification genEventExchangeSpec() {
        return ExchangeSpecification.exchange(eventStoreExchange)
                .type("topic")
                .durable(true);
    }

    public QueueSpecification genEventQueueSpecs(String queueName) {
        return QueueSpecification.queue(queueName)
                .durable(true);
    }

    public List<BindingSpecification> genEvenBindSpecs(String queueNameToBind, List<String> routingKeys) {
        if (routingKeys.isEmpty())
            return List.of();

        return routingKeys.stream()
                .map(routingKey -> BindingSpecification.binding(eventStoreExchange, routingKey, queueNameToBind))
                .toList();
    }

}

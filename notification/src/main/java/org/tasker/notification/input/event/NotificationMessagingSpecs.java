package org.tasker.notification.input.event;


import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tasker.common.models.event.NotificationCreatedEvent;
import org.tasker.common.models.event.NotificationInvalidatedEvent;
import reactor.core.publisher.Flux;
import reactor.rabbitmq.*;

@Slf4j
@Service
@Getter
public final class NotificationMessagingSpecs {

    private final String requestQueueName;
    private final String requestExchangeName;
    private final String responseExchangeName;
    private final String updatesExchangeName;
    private final String updatesQueueName;

    private final Sender sender;
    private final Receiver receiver;

    public NotificationMessagingSpecs(Sender sender, Receiver receiver,
                                      @Value("${communication.notification.request-queue}") String requestQueueName,
                                      @Value("${communication.notification.request-exchange}") String requestExchangeName,
                                      @Value("${communication.notification.response-exchange}") String responseExchangeName,
                                      @Value("${communication.notification.updates-queue}") String updatesQueueName,
                                      @Value("${communication.notification.updates-exchange}") String updatesExchangeName) {
        this.requestQueueName = requestQueueName;
        this.requestExchangeName = requestExchangeName;
        this.responseExchangeName = responseExchangeName;
        this.updatesQueueName = updatesQueueName;
        this.updatesExchangeName = updatesExchangeName;
        this.sender = sender;
        this.receiver = receiver;
    }

    @PostConstruct
    public void init() {
        // TODO: add sender and retriever bean retrieving
        log.debug("Queue initializing...");

        var queue = this.requestExchangeSpec();
        var exchange = this.requestQueueSpec();
        sender.declareExchange(queue)
                .then(sender.declareQueue(exchange))
                // todo: add correct routing keys
                .thenMany(Flux.just(NotificationCreatedEvent.NOTIFICATION_CREATED_V1, NotificationInvalidatedEvent.NOTIFICATION_INVALIDATED_V1)
                        .map(this::requestBindingSpecs)
                        .flatMap(sender::bind))
                .doOnError(ex -> log.error("Error while initializing command queue: {}", ex.getMessage()))
                .subscribe();

        var updatesQueue = this.updatesQueueSpec();
        var updatesExchange = this.updatesExchangeSpec();
        sender.declareExchange(updatesExchange)
                .then(sender.declareQueue(updatesQueue))
                .then(sender.bind(BindingSpecification.binding(updatesExchangeName, updatesExchangeName, updatesQueueName)))
                .doOnError(ex -> log.error("Error while initializing updates queue: {}", ex.getMessage()))
                .subscribe();
    }

    public QueueSpecification requestQueueSpec() {
        return QueueSpecification.queue(requestQueueName)
                .durable(true)
                .exclusive(false)
                .autoDelete(false);
    }

    public ExchangeSpecification requestExchangeSpec() {
        return ExchangeSpecification.exchange(requestExchangeName)
                .type("direct")
                .durable(true)
                .autoDelete(false);
    }

    public BindingSpecification requestBindingSpecs(String routingKey) {
        return BindingSpecification.binding(requestExchangeName, routingKey, requestQueueName);
    }

    public QueueSpecification updatesQueueSpec() {
        return QueueSpecification.queue(updatesQueueName)
                .durable(true)
                .exclusive(false)
                .autoDelete(false);
    }

    public ExchangeSpecification updatesExchangeSpec() {
        return ExchangeSpecification.exchange(updatesExchangeName)
                .type("direct")
                .durable(true)
                .autoDelete(false);
    }
}

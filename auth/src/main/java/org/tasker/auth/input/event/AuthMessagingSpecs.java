package org.tasker.auth.input.event;


import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.BindingSpecification;
import reactor.rabbitmq.ExchangeSpecification;
import reactor.rabbitmq.QueueSpecification;

@Slf4j
@Service
@Getter
public final class AuthMessagingSpecs {

    private final String requestQueueName;
    private final String requestExchangeName;
    private final String responseExchangeName;

    public AuthMessagingSpecs(@Value("${communication.auth.request-queue}") String requestQueueName,
                              @Value("${communication.auth.request-exchange}") String requestExchangeName,
                              @Value("${communication.auth.response-exchange}") String responseExchangeName) {
        this.requestQueueName = requestQueueName;
        this.requestExchangeName = requestExchangeName;
        this.responseExchangeName = responseExchangeName;

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
}

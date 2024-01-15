package org.tasker.updates.output.event;

import com.rabbitmq.client.AMQP;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.tasker.common.es.SerializerUtils;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.*;

import java.util.UUID;

@Slf4j
@Service
public class AuthPublisher {

    private final Receiver receiver;
    private final Sender sender;

    private final String requestExchangeName;
    private final String responseExchangeName;
    private final String responseQueueName;

    public AuthPublisher(Receiver receiver, Sender sender,
                         @Value("${communication.auth.request-exchange}") String requestExchangeName,
                         @Value("${communication.auth.response-exchange}") String responseExchangeName,
                         @Value("${communication.auth.response-queue}") String responseQueueName) {
        this.receiver = receiver;
        this.sender = sender;
        this.requestExchangeName = requestExchangeName;
        this.responseExchangeName = responseExchangeName;
        this.responseQueueName = responseQueueName;

        sender.declareExchange(ExchangeSpecification.exchange(responseExchangeName))
                .subscribe();
    }

    public Mono<byte[]> publishAndReceive(String actionName, Object messageBody) {
        final var correlationID = UUID.randomUUID().toString();
        final var responseQueue = responseQueueName + "." + correlationID;
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
                .correlationId(correlationID)
                .replyTo(correlationID)
                .build();

        return sender.declareQueue(QueueSpecification.queue(responseQueue).exclusive(true).autoDelete(true))
                .then(sender.bind(BindingSpecification.binding(responseExchangeName, correlationID, responseQueue)))
                .then(sender.send(Mono.just(new OutboundMessage(requestExchangeName, actionName, properties, SerializerUtils.serializeToJsonBytes(messageBody))))
                        .doOnSuccess(v -> log.info("Sent message to {}: {}", requestExchangeName, messageBody)))
                .thenMany(receiver.consumeAutoAck(responseQueue))
                .next()
                .map(delivery -> {
                    log.info("Received response on {}: {}", correlationID, delivery);
                    return delivery.getBody();
                });
    }


}

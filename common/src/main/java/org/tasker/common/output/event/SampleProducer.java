package org.tasker.common.output.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.rabbitmq.OutboundMessage;
import reactor.rabbitmq.QueueSpecification;
import reactor.rabbitmq.Sender;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleProducer {

    private final Sender sender;

    public void sendMessage() {
        String queue = "sample";
        String message = "Hello, JMS!";

        Publisher<OutboundMessage> outbound = Mono.just(new OutboundMessage("", queue, message.getBytes()));

        sender.declareQueue(QueueSpecification.queue(queue))
                .then(sender.send(outbound))
                .doOnSubscribe(s -> log.info("Sending message: {}", message))
                .doOnError(e -> log.error("Error sending message", e))
                .subscribe();
    }
}

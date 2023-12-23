package org.tasker.common.input.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.rabbitmq.Receiver;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleConsumer {

    private final Receiver receiver;

    public void processMessage() {
        receiver.consumeAutoAck("sample")
                .subscribe(delivery -> {
                    String message = new String(delivery.getBody());
                    log.info("Received message: {}", message);
                });
    }

}

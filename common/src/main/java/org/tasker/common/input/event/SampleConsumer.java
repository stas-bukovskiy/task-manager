package org.tasker.common.input.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SampleConsumer {

    @JmsListener(destination = "sample")
    public void processMessage(String content) {
        log.info("Received message: {}", content);
    }

}

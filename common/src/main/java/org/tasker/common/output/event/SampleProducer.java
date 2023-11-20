package org.tasker.common.output.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SampleProducer {

    private final JmsTemplate jmsTemplate;

    @Autowired
    public SampleProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
    }

    public void sendMessage() {
        String destination = "sample";
        String message = "Hello, JMS!";

        log.info("Sending message: {}", message);

        jmsTemplate.convertAndSend(destination, message);
    }
}

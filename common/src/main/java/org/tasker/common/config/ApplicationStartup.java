package org.tasker.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.tasker.common.input.event.SampleConsumer;
import org.tasker.common.output.event.SampleProducer;

@Profile("!test")
@Component
@RequiredArgsConstructor
public class ApplicationStartup {

    private final SampleProducer sampleProducer;
    private final SampleConsumer sampleConsumer;

    @EventListener(ApplicationReadyEvent.class)
    public void startProducer() {
        Thread producerThread = new Thread(() -> {
            while (true) {
                sampleProducer.sendMessage();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });

        producerThread.start();
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startConsumer() {
        sampleConsumer.processMessage();
    }

}
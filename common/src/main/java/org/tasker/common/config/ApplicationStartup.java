package org.tasker.common.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.tasker.common.output.event.SampleProducer;

@Component
@RequiredArgsConstructor
public class ApplicationStartup {

    private final SampleProducer sampleProducer;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        // This method will be invoked when the application is ready
        // You can perform any initialization tasks here
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

}
package org.tasker.updates.output.event;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Service
public class TaskCommunicator extends Communicator {
    public TaskCommunicator(Receiver receiver, Sender sender,
                            @Value("${communication.task.request-exchange}") String requestExchangeName,
                            @Value("${communication.task.response-exchange}") String responseExchangeName,
                            @Value("${communication.task.response-queue}") String responseQueueName) {
        super(receiver, sender, requestExchangeName, responseExchangeName, responseQueueName);
    }
}
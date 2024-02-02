package org.tasker.updates.output.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.rabbitmq.Receiver;
import reactor.rabbitmq.Sender;

@Slf4j
@Service
public class AuthCommunicator extends Communicator {

    public AuthCommunicator(Receiver receiver, Sender sender,
                            @Value("${communication.auth.request-exchange}") String requestExchangeName,
                            @Value("${communication.auth.response-exchange}") String responseExchangeName,
                            @Value("${communication.auth.response-queue}") String responseQueueName) {
        super(receiver, sender, requestExchangeName, responseExchangeName, responseQueueName);
    }

}

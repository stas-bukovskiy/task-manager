package org.tasker.updates.input.ws;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

public class ServerLogic {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicBoolean newClient;

    public ServerLogic() {
        newClient = new AtomicBoolean(true);
    }

    public Mono<Void> doLogic(WebSocketSession session, long interval) {
        return
                session
                        .receive()
                        .doOnNext(message -> {
                            if (newClient.get()) {
                                logger.info("Server -> client connected id=[{}]", session.getId());
                            }
                        })
                        .map(WebSocketMessage::getPayloadAsText)
                        .doOnNext(message -> logger.info("Server -> received from client id=[{}]: [{}]", session.getId(), message))
                        .filter(message -> newClient.get())
                        .doOnNext(message -> newClient.set(false))
                        .flatMap(message -> sendAtInterval(session, interval))
                        .then();
    }

    private Flux<Void> sendAtInterval(WebSocketSession session, long interval) {
        return
                Flux
                        .interval(Duration.ofMillis(interval))
                        .map(value -> Long.toString(value))
                        .flatMap(message ->
                                session
                                        .send(Mono.fromCallable(() -> session.textMessage(message)))
                                        .then(
                                                Mono
                                                        .fromRunnable(() -> logger.info("Server -> sent: [{}] to client id=[{}]", message, session.getId()))
                                        )
                        );
    }
}
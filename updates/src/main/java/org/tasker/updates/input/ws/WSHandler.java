
package org.tasker.updates.input.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.server.ResponseStatusException;
import org.tasker.common.es.SerializerUtils;
import org.tasker.updates.models.request.WSRequest;
import org.tasker.updates.models.response.ErrorMessages;
import org.tasker.updates.models.response.ErrorResponse;
import org.tasker.updates.models.response.WSResponse;
import org.tasker.updates.service.UserService;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class WSHandler implements WebSocketHandler {

    private final UserService userService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                .map(requestRaw -> {
                    var payload = requestRaw.getPayload();
                    var payloadBytes = new byte[payload.readableByteCount()];
                    payload.read(payloadBytes);

                    return SerializerUtils.deserializeFromJsonBytes(payloadBytes, WSRequest.class);
                })
                .flatMap(wsRequest -> delegateRequest(session, wsRequest)
                        .onErrorResume(ex -> {
                            log.error("Error while handling WS request: {}", wsRequest, ex);
                            return sendErrorResponse(session, wsRequest, ex);
                        }))
                .then();
    }

    private Mono<Void> delegateRequest(WebSocketSession session, WSRequest wsRequest) {
        switch (wsRequest.type()) {
            case "get_me" -> {
                return Mono.deferContextual(ctx -> {
                            final String currentUserId = ctx.get("aggregate_id");
                            return userService.getUserByAggregateId(currentUserId);
                        })
                        .flatMap(userBytes -> sendResponse(session, wsRequest, userBytes));
            }
            default -> {
                log.error("Invalid request type: {}", wsRequest.type());
                return Mono.empty();
            }
        }
    }

    private <T> Mono<Void> sendResponse(WebSocketSession session, WSRequest wsRequest, T data) {
        return session.send(Mono.just(session.textMessage(SerializerUtils.serializeToJsonString(
                new WSResponse<>(wsRequest.correlationId(), data)
        ))));
    }

    private Mono<Void> sendErrorResponse(WebSocketSession session, WSRequest wsRequest, Throwable ex) {
        ErrorResponse errorResponse;
        if (ex instanceof ResponseStatusException) {
            errorResponse = ErrorResponse.builder()
                    .status(((ResponseStatusException) ex).getStatusCode().value())
                    .message(ex.getMessage())
                    .timestamp(System.currentTimeMillis())
                    .correlationId(wsRequest.correlationId())
                    .build();
        } else {
            errorResponse = ErrorResponse.builder()
                    .status(500)
                    .message(ErrorMessages.INTERNAL_SERVER_ERROR)
                    .timestamp(System.currentTimeMillis())
                    .correlationId(wsRequest.correlationId())
                    .build();
        }

        return session.send(Mono.just(session.textMessage(SerializerUtils.serializeToJsonString(errorResponse))));
    }

}
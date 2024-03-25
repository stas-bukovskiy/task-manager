package org.tasker.updates.input.ws;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.NettyDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.AbstractServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.HandshakeInfo;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.adapter.NettyWebSocketSessionSupport;
import org.springframework.web.reactive.socket.adapter.ReactorNettyWebSocketSession;
import org.springframework.web.reactive.socket.server.RequestUpgradeStrategy;
import org.springframework.web.server.ServerWebExchange;
import org.tasker.updates.exceptions.NotAuthenticatedException;
import org.tasker.updates.models.request.VerifyTokenRequest;
import org.tasker.updates.service.AuthService;
import reactor.core.publisher.Mono;
import reactor.netty.http.server.HttpServerResponse;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthRequestUpgradeStrategy implements RequestUpgradeStrategy {

    private final int maxFramePayloadLength = NettyWebSocketSessionSupport.DEFAULT_FRAME_MAX_SIZE;

    private final AuthService authService;

    private static HttpServerResponse getNativeResponse(ServerHttpResponse response) {
        if (response instanceof AbstractServerHttpResponse) {
            return ((AbstractServerHttpResponse) response).getNativeResponse();
        } else if (response instanceof ServerHttpResponseDecorator) {
            return getNativeResponse(((ServerHttpResponseDecorator) response).getDelegate());
        } else {
            throw new IllegalArgumentException("Couldn't find native response in " + response.getClass().getName());
        }
    }

    @Override
    public Mono<Void> upgrade(ServerWebExchange exchange,
                              WebSocketHandler handler,
                              @Nullable String subProtocol,
                              Supplier<HandshakeInfo> handshakeInfoFactory) {

        ServerHttpResponse response = exchange.getResponse();
        HttpServerResponse reactorResponse = getNativeResponse(response);
        HandshakeInfo handshakeInfo = handshakeInfoFactory.get();
        NettyDataBufferFactory bufferFactory = (NettyDataBufferFactory) response.bufferFactory();

        List<String> authHeaders = handshakeInfo.getHeaders().get(HttpHeaders.AUTHORIZATION);
        return Mono.justOrEmpty(authHeaders)
                .switchIfEmpty(Mono.error(new NotAuthenticatedException("The 'Authorization' header is missed")))
                .handle((headers, sink) -> {
                    if (headers.isEmpty()) {
                        sink.error(new NotAuthenticatedException("The 'Authorization' header is missed"));
                        return;
                    }

                    sink.next(headers.get(0));
                })
                .cast(String.class)
                .map(header -> header.replace("Bearer ", "").trim())
                .flatMap(token -> authService.verifyToken(VerifyTokenRequest.builder()
                        .token(token)
                        .build()))
                .flatMap(authRes -> {
                    if (authRes.getHttpCode() != HttpStatus.OK.value()) {
                        response.setRawStatusCode(authRes.getHttpCode());
                        return response.setComplete();
                    }

                    return reactorResponse.sendWebsocket((in, out) -> {
                        ReactorNettyWebSocketSession session = new ReactorNettyWebSocketSession(in, out, handshakeInfo, bufferFactory, this.maxFramePayloadLength);
                        session.getAttributes().put("aggregate_id", authRes.getData());
                        return handler.handle(session)
                                .contextWrite(ctx -> ctx.put("aggregate_id", authRes.getData()));
                    });
                })
                .onErrorResume((ex) -> {
                    if (ex instanceof NotAuthenticatedException) {
                        log.info("Token validation is failed: {}", ex.getMessage());
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    } else {
                        log.error("Error occurred when verified token", ex);
                        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    }

                    return response.setComplete();
                })
                .then();
    }

}
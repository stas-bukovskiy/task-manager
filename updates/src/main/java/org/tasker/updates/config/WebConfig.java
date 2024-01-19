package org.tasker.updates.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.tasker.updates.input.http.AuthHandler;

import java.util.Arrays;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Slf4j
@Configuration
@EnableWebFlux
public class WebConfig implements WebFluxConfigurer {

    @Value("${allowed.origins}")
    private String[] allowedOrigins;

    @Bean
    RouterFunction<ServerResponse> routerFunction(AuthHandler handler) {
        return route()
                .POST("api/v1/auth/sign-up", accept(APPLICATION_JSON), handler::registerNewUser)
                .POST("api/v1/auth/sign-in", accept(APPLICATION_JSON), handler::loginUser)
                .POST("api/v1/auth/verify", accept(APPLICATION_JSON), handler::verifyToken)
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        log.info("Allowed origins: {}", Arrays.toString(allowedOrigins));

        corsRegistry.addMapping("/**")
                .allowedOrigins(allowedOrigins)
                .allowedMethods("POST", "OPTIONS", "HEAD")
                .maxAge(3600);
    }

}

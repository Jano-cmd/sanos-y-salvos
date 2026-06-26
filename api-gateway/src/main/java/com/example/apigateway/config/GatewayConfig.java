package com.example.apigateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class GatewayConfig {

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .uri("http://auth-service:8081"))
                .route("user-service", r -> r.path("/users/**")
                        .uri("http://user-service:8082"))
                .route("pet-service", r -> r.path("/pets/**")
                        .uri("http://pet-service:8083"))
                .route("report-service", r -> r.path("/reports/**")
                        .uri("http://report-service:8084"))
                .route("match-service", r -> r.path("/matches/**")
                        .uri("http://match-service:8085"))
                .route("notification-service", r -> r.path("/notifications/**")
                        .uri("http://notification-service:8086"))
                .build();
    }

}

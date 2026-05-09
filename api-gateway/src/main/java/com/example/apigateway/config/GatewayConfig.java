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
                        .uri("http://localhost:8081"))
                .route("user-service", r -> r.path("/users/**")
                        .uri("http://localhost:8082"))
                .route("pet-service", r -> r.path("/pets/**")
                        .uri("http://localhost:8083"))
                .route("report-service", r -> r.path("/reports/**")
                        .uri("http://localhost:8084"))
                .build();
    }

}
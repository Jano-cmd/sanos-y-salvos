package com.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;

@Configuration
public class GatewayConfig {

    @Value("${services.auth.url}")
    private String authServiceUrl;

    @Value("${services.user.url}")
    private String userServiceUrl;

    @Value("${services.pet.url}")
    private String petServiceUrl;

    @Value("${services.report.url}")
    private String reportServiceUrl;

    @Value("${services.match.url}")
    private String matchServiceUrl;

    @Value("${services.notification.url}")
    private String notificationServiceUrl;

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("auth-service", r -> r.path("/auth/**")
                        .uri(authServiceUrl))
                .route("user-service", r -> r.path("/users/**")
                        .uri(userServiceUrl))
                .route("pet-service", r -> r.path("/pets/**")
                        .uri(petServiceUrl))
                .route("report-service", r -> r.path("/reports/**")
                        .uri(reportServiceUrl))
                .route("match-service", r -> r.path("/matches/**")
                        .uri(matchServiceUrl))
                .route("notification-service", r -> r.path("/notifications/**")
                        .uri(notificationServiceUrl))
                .build();
    }

}

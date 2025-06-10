package com.thomasvitale.edgeservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    @Bean
    public CustomGatewayFilter customGatewayFilterFactory() {
        return new CustomGatewayFilter();
    }
}
package com.thomasvitale.edgeservice;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.GatewayFilterFactory;

public class CustomGatewayFilter implements GatewayFilterFactory<CustomGatewayFilter.Config> {

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            System.out.println("Filtro dinamico applicato con parametro: " + config.getMyParam());
            return chain.filter(exchange);
        };
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    public static class Config {
        private String myParam;

        public String getMyParam() {
            return myParam;
        }

        public void setMyParam(String myParam) {
            this.myParam = myParam;
        }
    }
}


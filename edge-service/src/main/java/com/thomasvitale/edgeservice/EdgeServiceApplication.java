package com.thomasvitale.edgeservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.autoconfigure.security.reactive.EndpointRequest;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class EdgeServiceApplication {

    public static void main(String[] args) {
        Hooks.enableAutomaticContextPropagation();
        SpringApplication.run(EdgeServiceApplication.class, args);
    }

    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public EdgeServiceApplication(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(exchange -> exchange.matchers(EndpointRequest.toAnyEndpoint()).permitAll()
                        .anyExchange().authenticated())
                .oauth2Login(Customizer.withDefaults())
                .logout(l -> l.logoutSuccessHandler(new CustomLogoutSuccessHandler(authorizedClientService)))
                //.oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(new CustomAuthenticationSuccessHandler(authorizedClientService)))
                .build();
    }
}

@RestController
class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping("/books-fallback")
    Flux<Void> getBooksFallback() {
        log.info("Fallback for book service");
        return Flux.empty();
    }

}

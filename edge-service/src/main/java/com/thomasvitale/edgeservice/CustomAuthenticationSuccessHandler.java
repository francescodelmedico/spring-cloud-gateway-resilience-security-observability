package com.thomasvitale.edgeservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.OAuth2RefreshToken;
import reactor.core.publisher.Mono;

import java.net.URI;

public class CustomAuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(CustomAuthenticationSuccessHandler.class);
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public CustomAuthenticationSuccessHandler(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        if (authentication instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;

            return authorizedClientService
                    .loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName())
                    .flatMap(authorizedClient -> {
                        log.info(authorizedClient.getAccessToken().getTokenValue());
                        OAuth2RefreshToken refreshToken = authorizedClient.getRefreshToken();

                        return webFilterExchange.getExchange().getSession()
                                                .doOnNext(session -> {
                                                    if (refreshToken != null) {
                                                        session.getAttributes().put("refresh_token", refreshToken.getTokenValue());
                                                    }
                                                })
                                                .then(redirectTo(webFilterExchange, "/home")); // 🔁 Redirect dopo salvataggio sessione
                    });
        }
        return redirectTo(webFilterExchange, "/login?error");
    }

    private Mono<Void> redirectTo(WebFilterExchange webFilterExchange, String location) {
        ServerHttpResponse response = webFilterExchange.getExchange().getResponse();
        response.setStatusCode(HttpStatus.FOUND); // 302
        response.getHeaders().setLocation(URI.create(location));
        return response.setComplete(); // chiude la risposta
    }
}

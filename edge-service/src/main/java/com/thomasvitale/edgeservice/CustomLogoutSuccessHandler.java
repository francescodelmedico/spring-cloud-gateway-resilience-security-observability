package com.thomasvitale.edgeservice;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;

public class CustomLogoutSuccessHandler implements ServerLogoutSuccessHandler {

    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    public CustomLogoutSuccessHandler(ReactiveOAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @Override
    public Mono<Void> onLogoutSuccess(WebFilterExchange exchange, Authentication authentication) {
        if (authentication == null) {
            return exchange.getExchange().getResponse().setComplete();
        }

        String clientRegistrationId = "keycloak"; // o il nome definito nel tuo application.yml

        return authorizedClientService.loadAuthorizedClient(clientRegistrationId, authentication.getName()).flatMap(client -> {
            //String idToken = client.getAccessToken().getTokenValue(); // ⚠️ questo è l'access token

            OAuth2AuthenticationToken oauth2Token = (OAuth2AuthenticationToken) authentication;
            OidcUser oidcUser = (OidcUser) oauth2Token.getPrincipal();
            String idTokenHint = oidcUser.getIdToken().getTokenValue(); // ✅ questo è l'id_token

            String issuerUri = client.getClientRegistration().getProviderDetails().getIssuerUri();
            String logoutUri = issuerUri + "/protocol/openid-connect/logout" + "?id_token_hint=" + idTokenHint + "&post_logout_redirect_uri=http://localhost:9000/books";

            ServerHttpResponse response = exchange.getExchange().getResponse();
            response.setStatusCode(HttpStatus.FOUND);
            response.getHeaders().setLocation(URI.create(logoutUri));

            // Invalida il cookie SESSION
            ResponseCookie cookie = ResponseCookie.from("SESSION", "").path("/").maxAge(Duration.ZERO).httpOnly(true).build();

            response.addCookie(cookie);

            return response.setComplete();
        });
    }
}

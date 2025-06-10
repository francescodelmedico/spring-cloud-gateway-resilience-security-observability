package com.thomasvitale.bookservice;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CustomJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        Collection<GrantedAuthority> authorities = extractAuthorities(jwt);
        AbstractAuthenticationToken authToken = new JwtAuthenticationToken(jwt, authorities);
        return authToken;
    }

    private Collection<GrantedAuthority> extractAuthorities(Jwt jwt) {
        return Stream.concat(
                extractRealmRoles(jwt).stream(),
                extractClientRoles(jwt).stream()
        ).collect(Collectors.toSet());
    }

    private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
        Map<String, Object> realmAccess = jwt.getClaim("realm_access");
        if (realmAccess != null && realmAccess.containsKey("roles")) {
            @SuppressWarnings("unchecked")
            Collection<String> roles = (Collection<String>) realmAccess.get("roles");
            return roles.stream()
                        .map(role -> "ROLE_" + role.toUpperCase())
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toSet());
        }
        return Set.of();
    }

    private Collection<GrantedAuthority> extractClientRoles(Jwt jwt) {
        Map<String, Object> resourceAccess = jwt.getClaim("resource_access");
        if (resourceAccess != null && resourceAccess.containsKey("delsof-client")) {
            Map<String, Object> client = (Map<String, Object>) resourceAccess.get("delsof-client");
            if (client.containsKey("roles")) {
                @SuppressWarnings("unchecked")
                Collection<String> roles = (Collection<String>) client.get("roles");
                return roles.stream()
                            .map(role -> "ROLE_" + role.toUpperCase())
                            .map(SimpleGrantedAuthority::new)
                            .collect(Collectors.toSet());
            }
        }
        return Set.of();
    }
}
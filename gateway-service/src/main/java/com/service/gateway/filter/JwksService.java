package com.service.gateway.filter;

import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.stereotype.Component;


import java.net.URL;

@Component
public class JwksService {
    private final JWKSource<SecurityContext> jwkSource;

    public JwksService() throws Exception {
        // Auth service JWKS endpoint
        URL jwksURL = new URL("http://auth-service:8081/.well-known/jwks.json");
        this.jwkSource = new RemoteJWKSet<>(jwksURL);
    }

    public JWKSource<SecurityContext> getJwkSource() {
        return jwkSource;
    }
}

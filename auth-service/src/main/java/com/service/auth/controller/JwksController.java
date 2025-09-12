package com.service.auth.controller;

import com.service.auth.security.JwksProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JwksController {
    private final JwksProvider jwksProvider;

    public JwksController(JwksProvider jwksProvider) {
        this.jwksProvider = jwksProvider;
    }

    @GetMapping("/.well-known/jwks.json")
    public Object keys() throws Exception {
        return jwksProvider.getJwks().toJSONObject();
    }
}
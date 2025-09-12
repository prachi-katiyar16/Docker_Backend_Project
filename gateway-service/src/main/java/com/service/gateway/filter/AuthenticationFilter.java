package com.service.gateway.filter;

import com.nimbusds.jose.jwk.JWKMatcher;
import com.nimbusds.jose.jwk.JWKSelector;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.SignedJWT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    private final RouteValidator validator;
    private final JWKSource<SecurityContext> jwkSource;

    public AuthenticationFilter(RouteValidator validator, JwksService jwksService) {
        super(Config.class);
        this.validator = validator;
        this.jwkSource = jwksService.getJwkSource();
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            if (validator.isSecured.test(request)) {
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }

                String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
                String token = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                } else {
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                logger.info(token);
                try {
                    SignedJWT signedJWT = SignedJWT.parse(token);

                    // Validate expiry
                    Date exp = signedJWT.getJWTClaimsSet().getExpirationTime();
                    logger.info("expiration time {}",exp);
                    System.out.println(exp);
                    if (exp.before(new Date())) {
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.setComplete();
                    }

                    // Select key based on "kid" from JWT header
                    String kid = signedJWT.getHeader().getKeyID();
                    logger.info("KID {}",kid);
                    JWKSelector selector = new JWKSelector(new JWKMatcher.Builder().keyID(kid).build());
                    List<com.nimbusds.jose.jwk.JWK> jwks = jwkSource.get(selector, null);
                    logger.info("JWKS selector {}",jwks);
                    if (jwks.isEmpty() || !(jwks.get(0) instanceof RSAKey rsaKey)) {
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.setComplete();
                    }

                    // Verify signature
                    if (!signedJWT.verify(new com.nimbusds.jose.crypto.RSASSAVerifier(rsaKey.toRSAPublicKey()))) {
                        response.setStatusCode(HttpStatus.UNAUTHORIZED);
                        return response.setComplete();
                    }

                    // Extract custom claims
                    Long userId = signedJWT.getJWTClaimsSet().getLongClaim("id");
                    String role = signedJWT.getJWTClaimsSet().getStringClaim("role");

                    // Mutate request & forward
                    ServerHttpRequest newRequest = request.mutate()
                            .header("X-Authenticated-Id", String.valueOf(userId))
                            .header("X-Authenticated-Role", role)
                            .build();
                    logger.info("Token is valid{} and  {}",userId,role);
                    return chain.filter(exchange.mutate().request(newRequest).build());

                } catch (ParseException e) {
                    logger.error("Invalid token format: {}", e.getMessage());
                    response.setStatusCode(HttpStatus.BAD_REQUEST);
                    return response.setComplete();
                } catch (Exception e) {
                    logger.error("Token validation failed: {}", e.getMessage());
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
            }
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
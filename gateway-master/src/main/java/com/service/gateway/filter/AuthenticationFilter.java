package com.service.gateway.filter;


import com.example.common.dto.UserAuthDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;


@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    @Autowired
    private RouteValidator validator;

    @Autowired
    private RestTemplate template;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        System.out.println("***************");
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();
            String path = request.getURI().getPath();

            if (validator.isSecured.test(request)) {
                logger.info("Secured endpoint hit: {}", path);
                if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    logger.warn("Missing authorization header for secured endpoint: {}", path);
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }

                String authHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
                String token = null;
                if (authHeader != null && authHeader.startsWith("Bearer ")) {
                    token = authHeader.substring(7);
                    logger.info("Extracted token from header for endpoint: {}", path);
                } else {
                    logger.warn("Invalid Authorization header format for endpoint: {}", path);
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }
                System.out.println(token);

                try {
                    logger.info("Calling auth service for validation with token for endpoint: {}", path);
                    UserAuthDetails authDetails=null;
                    authDetails = template.getForObject(
                            "http://localhost:8081/auth/validate?token=" + token,
                            UserAuthDetails.class
                    );
                    logger.info("auth details"+ authDetails);

                    logger.info("Successfully validated token. User details received for endpoint: {}", path);

                    // Create a new request object with the custom headers
                    ServerHttpRequest newRequest = request.mutate()
                            .header("X-Authenticated-Role", authDetails.getRole())
                            .header("X-Authenticated-Id", String.valueOf(authDetails.getId()))
                            .build();

                    logger.info("Header 'X-Authenticated-Role' added to request with value '{}' for endpoint: {}", authDetails.getRole(), path);

                    // THIS IS THE CRITICAL FIX: Pass the mutated request down the chain
                    return chain.filter(exchange.mutate().request(newRequest).build());

                } catch (HttpClientErrorException e) {
                    logger.error("Invalid token. Auth service returned: {} - {} for endpoint: {}", e.getStatusCode(), e.getResponseBodyAsString(), path);
                    response.setStatusCode(e.getStatusCode());
                    return response.setComplete();
                } catch (Exception e) {
                    logger.error("Error during authentication for endpoint {}: {}", path, e.getMessage());
                    response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return response.setComplete();
                }
            }
            // Continue the filter chain for non-secured (public) endpoints.
            logger.info("Public endpoint hit. Bypassing authentication for: {}", path);
            return chain.filter(exchange);
        };
    }

    public static class Config {
    }
}
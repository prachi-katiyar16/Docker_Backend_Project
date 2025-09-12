package com.service.auth.security;



import com.example.common.dto.UserAuthDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;


@Component
public class JwtUtil {

    @Autowired
    RsaKeyLoader keyLoader;



    public String generateToken(Authentication authentication) {
        try {
            CustomUserDetails user = (CustomUserDetails) authentication.getPrincipal();
            PrivateKey privateKey = keyLoader.loadPrivateKey();

            return Jwts.builder()
                    .setHeaderParam("kid", "my-key-id")
                    .setSubject(user.getUsername())
                    .claim("id", user.getId())
                    .claim("role", user.getAuthorities().stream()
                            .findFirst().map(GrantedAuthority::getAuthority)
                            .orElse("ROLE_USER"))
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                    .signWith(privateKey, SignatureAlgorithm.RS256) // ✅ RS256 with private key
                    .compact();

        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    public UserAuthDetails validateAndExtractDetails(String token) {
        try {
            PublicKey publicKey = keyLoader.loadPublicKey();
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(publicKey) // ✅ verify with public key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            UserAuthDetails details = new UserAuthDetails();
            details.setId(claims.get("id", Long.class));
            details.setRole(claims.get("role", String.class));
            return details;

        } catch (Exception e) {
            throw new RuntimeException("Invalid JWT token", e);
        }
    }
}

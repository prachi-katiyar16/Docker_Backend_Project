package com.service.auth.security;



import com.example.common.dto.UserAuthDetails;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.context.annotation.Bean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;


@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;

//



    public UserAuthDetails validateAndExtractDetails(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSignKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        UserAuthDetails details = new UserAuthDetails();
        details.setId(claims.get("id",Long.class));   // usually the username
        details.setRole(claims.get("role", String.class)); // custom claim
        return details;
    }


    public String generateToken(Authentication authentication) {

        CustomUserDetails userPrincipal = (CustomUserDetails) authentication.getPrincipal();
        return Jwts.builder()


                .claim("id",userPrincipal.getId())
                .claim("role",userPrincipal.getAuthorities().stream().findFirst().map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER"))
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 30))
                .signWith( SignatureAlgorithm.HS256,getSignKey()).compact();

    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}

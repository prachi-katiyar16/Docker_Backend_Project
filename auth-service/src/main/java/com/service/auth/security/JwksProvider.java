package com.service.auth.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;

@Component
public class JwksProvider {

    private static final String PUBLIC_KEY_PATH = "certs/public.pem";

    public JWKSet getJwks() throws Exception {
        RSAPublicKey publicKey = loadPublicKey();
        JWK jwk = new RSAKey.Builder(publicKey)
                .keyID("my-key-id") // 🔑 useful if you rotate keys later
                .build();

        return new JWKSet(Collections.singletonList(jwk));
    }

    private RSAPublicKey loadPublicKey() throws Exception {
        // load resource from classpath
        ClassPathResource resource = new ClassPathResource(PUBLIC_KEY_PATH);
        String pem = new String(resource.getInputStream().readAllBytes());

        // strip headers/footers and whitespace
        pem = pem.replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);

        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(spec);
    }
}
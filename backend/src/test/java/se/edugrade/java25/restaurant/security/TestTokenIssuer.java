package se.edugrade.java25.restaurant.security;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Stands in for Auth0 in tests: signs RS256 access tokens with the same claims
 * an Auth0 token for this API carries ({@code iss}, {@code aud}, {@code sub},
 * {@code iat}, {@code exp}, {@code scope} and the namespaced roles claim).
 *
 * <p>Test code only. The key pair is generated per instance, so each test
 * context has its own key, and {@link TestJwtDecoderConfig} trusts only the
 * public half of it.
 */
public class TestTokenIssuer {

    public static final String ISSUER = "https://test-issuer.restaurant.example/";
    public static final String AUDIENCE = "https://restaurant-api";
    public static final String ROLES_CLAIM = "https://restaurant-api/roles";

    private static final Duration LIFETIME = Duration.ofMinutes(5);

    private final RSAKey signingKey = generateKey();

    /** The public key set a resource server would fetch from the issuer's JWKS endpoint. */
    public JWKSet publishedKeySet() {
        return new JWKSet(signingKey.toPublicJWK());
    }

    /** A valid token from this issuer, for this API, with the given scope and roles. */
    public String token(String scope, String... roles) {
        Instant now = Instant.now();
        return mintToken(ISSUER, AUDIENCE, scope, List.of(roles), now, now.plus(LIFETIME));
    }

    private String mintToken(String issuer, String audience, String scope, List<String> roles,
            Instant issuedAt, Instant expiresAt) {
        return sign(claims(issuer, audience, scope, roles, issuedAt, expiresAt), signingKey);
    }

    private static JWTClaimsSet claims(String issuer, String audience, String scope, List<String> roles,
            Instant issuedAt, Instant expiresAt) {
        return new JWTClaimsSet.Builder()
                .issuer(issuer)
                .audience(audience)
                .subject("test-user")
                .issueTime(Date.from(issuedAt))
                .expirationTime(Date.from(expiresAt))
                .claim("scope", scope)
                .claim(ROLES_CLAIM, roles)
                .build();
    }

    private static String sign(JWTClaimsSet claims, RSAKey key) {
        try {
            SignedJWT jwt = new SignedJWT(
                    new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(),
                    claims);
            jwt.sign(new RSASSASigner(key));
            return jwt.serialize();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not sign test token", e);
        }
    }

    private static RSAKey generateKey() {
        try {
            return new RSAKeyGenerator(2048)
                    .keyID(UUID.randomUUID().toString())
                    .algorithm(JWSAlgorithm.RS256)
                    .generate();
        } catch (JOSEException e) {
            throw new IllegalStateException("Could not generate an RSA key pair for the test token issuer", e);
        }
    }
}

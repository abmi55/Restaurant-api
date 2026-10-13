package se.edugrade.java25.restaurant.security;

import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtAudienceValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtIssuerValidator;
import org.springframework.security.oauth2.jwt.JwtTimestampValidator;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

/**
 * Replaces Boot's Auth0 decoder in tests that send real HTTP requests.
 *
 * <p>The decoder is real: it verifies the RS256 signature against the test
 * issuer's public key and validates time claims, issuer and audience, just as
 * the Auth0 decoder does. Only the trusted key and issuer differ, so the tests
 * need no network.
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestJwtDecoderConfig {

    @Bean
    TestTokenIssuer testTokenIssuer() {
        return new TestTokenIssuer();
    }

    @Bean
    JwtDecoder jwtDecoder(TestTokenIssuer tokenIssuer) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSource(new ImmutableJWKSet<>(tokenIssuer.publishedKeySet()))
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(),
                new JwtIssuerValidator(TestTokenIssuer.ISSUER),
                new JwtAudienceValidator(TestTokenIssuer.AUDIENCE)));
        return decoder;
    }
}

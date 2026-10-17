package se.edugrade.java25.restaurant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
public class CorsConfig {

    @Bean
    CorsConfigurationSource corsConfigurationSource(
            @Value("${app.cors.allowed-origins}") List<String> allowedOrigins) {
        CorsConfiguration configuration = new CorsConfiguration();
        // An origin includes scheme, host and port; localhost and 127.0.0.1 differ.
        // Wildcard origins with credentials would expose cookie-authenticated responses to any site.
        // Browsers reject that combination, and reflecting every Origin would recreate the risk.
        // Bearer tokens use a header, so credentials are disabled and only exact origins are allowed.
        // Spring rejects "*" with credentials, but only on the first request, not at startup.
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type", "If-None-Match"));
        configuration.setExposedHeaders(List.of("Location", "ETag"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(1800L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // A path without a rule would answer preflights with 200 but no CORS headers.
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}

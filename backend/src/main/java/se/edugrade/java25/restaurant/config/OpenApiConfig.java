package se.edugrade.java25.restaurant.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.security.OAuthFlow;
import io.swagger.v3.oas.models.security.OAuthFlows;
import io.swagger.v3.oas.models.security.Scopes;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    private static final String OAUTH_SCHEME = "auth0";

    @Bean
    OpenAPI openAPI(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        OAuthFlow authorizationCode = new OAuthFlow()
                .authorizationUrl(issuerUri + "authorize")
                .tokenUrl(issuerUri + "oauth/token")
                .scopes(new Scopes()
                        .addString("bookings:write", "Create bookings")
                        .addString("bookings:manage", "Confirm, seat and cancel bookings")
                        .addString("restaurants:write", "Create restaurants"));

        return new OpenAPI().components(new Components().addSecuritySchemes(OAUTH_SCHEME,
                new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().authorizationCode(authorizationCode))));
    }

    @Bean
    OpenApiCustomizer operationSecurity() {
        return openApi -> {
            secure(openApi, "/restaurants", false, "restaurants:write");
            secure(openApi, "/bookings", true);
            secure(openApi, "/bookings/{id}", true);
            secure(openApi, "/bookings", false, "bookings:write");
            secure(openApi, "/bookings/{id}/confirm", false, "bookings:manage");
            secure(openApi, "/bookings/{id}/seat", false, "bookings:manage");
            secure(openApi, "/bookings/{id}/cancel", false, "bookings:manage");
        };
    }

    private static void secure(OpenAPI openApi, String path, boolean get, String... scopes) {
        var pathItem = openApi.getPaths().get(path);
        if (pathItem == null) {
            return;
        }
        Operation operation = get ? pathItem.getGet() : pathItem.getPost();
        if (operation != null) {
            operation.addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME, List.of(scopes)));
        }
    }
}

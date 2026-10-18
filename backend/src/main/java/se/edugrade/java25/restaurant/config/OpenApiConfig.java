package se.edugrade.java25.restaurant.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
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

    /**
     * Deklarerar Auth0:s OAuth2-säkerhetsschema i OpenAPI-dokumentet.
     *
     * <p>Swagger UI använder authorization code-flödet mot den konfigurerade
     * issuern för att hämta en access token. Scopen som listas här är de som
     * användaren kan begära vid inloggning.
     */
    @Bean
    OpenAPI openAPI(@Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}") String issuerUri) {
        OAuthFlow authorizationCode = new OAuthFlow()
                .authorizationUrl(issuerUri + "authorize")
                .tokenUrl(issuerUri + "oauth/token")
                .scopes(new Scopes()
                        .addString("bookings:write", "Create bookings")
                        .addString("bookings:manage", "Confirm, seat and cancel bookings")
                        .addString("restaurants:write", "Create and update restaurants"));

        return new OpenAPI().components(new Components().addSecuritySchemes(OAUTH_SCHEME,
                new SecurityScheme()
                        .type(SecurityScheme.Type.OAUTH2)
                        .flows(new OAuthFlows().authorizationCode(authorizationCode))));
    }

    /**
     * Markerar de skyddade endpointsen i det genererade dokumentet, med det
     * scope som var och en kräver. Endpoints som inte listas här är publika.
     *
     * <p>Detta påverkar bara dokumentationen. Själva åtkomstreglerna styrs av
     * säkerhetskonfigurationen.
     */
    @Bean
    OpenApiCustomizer operationSecurity() {
        return openApi -> {
            secure(openApi, "/restaurants", PathItem.HttpMethod.POST, "restaurants:write");
            secure(openApi, "/restaurants/{id}", PathItem.HttpMethod.PUT, "restaurants:write");
            secure(openApi, "/bookings", PathItem.HttpMethod.GET);
            secure(openApi, "/bookings/{id}", PathItem.HttpMethod.GET);
            secure(openApi, "/bookings", PathItem.HttpMethod.POST, "bookings:write");
            secure(openApi, "/bookings/{id}/confirm", PathItem.HttpMethod.POST, "bookings:manage");
            secure(openApi, "/bookings/{id}/seat", PathItem.HttpMethod.POST, "bookings:manage");
            secure(openApi, "/bookings/{id}/cancel", PathItem.HttpMethod.POST, "bookings:manage");
        };
    }

    /**
     * Lägger till ett OAuth2-säkerhetskrav med de angivna scopen på en
     * operation. Utan scopes kräver operationen bara en giltig token.
     * Gör ingenting om pathen eller metoden saknas i dokumentet.
     */
    private static void secure(OpenAPI openApi, String path, PathItem.HttpMethod method, String... scopes) {
        var pathItem = openApi.getPaths().get(path);
        if (pathItem == null) {
            return;
        }
        Operation operation = pathItem.readOperationsMap().get(method);
        if (operation != null) {
            operation.addSecurityItem(new SecurityRequirement().addList(OAUTH_SCHEME, List.of(scopes)));
        }
    }
}

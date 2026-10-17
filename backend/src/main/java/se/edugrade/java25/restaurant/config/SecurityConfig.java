package se.edugrade.java25.restaurant.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.authorization.AuthorityAuthorizationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.security.roles-claim}") String rolesClaim) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(
                new RestaurantAuthoritiesConverter(rolesClaim));

        http
                // CorsFilter runs before authentication: a preflight never carries a token,
                // and a 401 response must still carry CORS headers for the browser to show it.
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/error", "/swagger-ui.html", "/swagger-ui/**",
                                "/v3/api-docs", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/health", "/restaurants", "/restaurants/*")
                                .permitAll()
                        .requestMatchers(HttpMethod.POST, "/restaurants")
                                .access(roleAndScope("ADMIN", "restaurants:write"))
                        .requestMatchers(HttpMethod.PUT, "/restaurants/*")
                                .access(roleAndScope("ADMIN", "restaurants:write"))
                        .requestMatchers(HttpMethod.GET, "/bookings", "/bookings/*")
                                .hasAnyRole("GUEST", "STAFF")
                        .requestMatchers(HttpMethod.POST, "/bookings")
                                .access(roleAndScope("GUEST", "bookings:write"))
                        .requestMatchers(HttpMethod.POST, "/bookings/*/confirm", "/bookings/*/seat",
                                "/bookings/*/cancel")
                                .access(roleAndScope("STAFF", "bookings:manage"))
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // CSRF-skydd behövs när webbläsaren skickar med inloggningen automatiskt, till exempel en sessionscookie.
                // Det här API:t är stateless och autentiserar bara med Authorization: Bearer. Webbläsaren skickar
                // aldrig den headern av sig själv, och ett främmande formulär kan inte sätta den. Inget kan
                // förfalskas, så CSRF är avstängt. Slå på CSRF igen om autentiseringen någon gång flyttas till cookies.
                .csrf(AbstractHttpConfigurer::disable)
                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }

    // The role says who the user is; the scope says what the client may do on the user's behalf.
    private static AuthorizationManager<RequestAuthorizationContext> roleAndScope(String role, String scope) {
        return AuthorizationManagers.allOf(
                AuthorityAuthorizationManager.hasRole(role),
                AuthorityAuthorizationManager.hasAuthority("SCOPE_" + scope));
    }
}

package se.edugrade.java25.restaurant.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;

public class RestaurantAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final DelegatingJwtGrantedAuthoritiesConverter delegate;

    /**
     * Bygger en converter som läser behörigheter från två claims i JWT:n.
     *
     * <p>{@code scope}-claimet blir {@code SCOPE_}-behörigheter, som i Springs
     * standardbeteende. Rollerna i claimet {@code rolesClaim} blir
     * {@code ROLE_}-behörigheter, så att de fungerar med {@code hasRole}.
     *
     * @param rolesClaim namnet på claimet som innehåller användarens roller
     */
    public RestaurantAuthoritiesConverter(String rolesClaim) {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(rolesClaim);
        roles.setAuthorityPrefix("ROLE_");
        delegate = new DelegatingJwtGrantedAuthoritiesConverter(
                new JwtGrantedAuthoritiesConverter(), roles);
    }

    /**
     * Returnerar både scopen och rollerna i token som behörigheter.
     */
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return delegate.convert(jwt);
    }
}

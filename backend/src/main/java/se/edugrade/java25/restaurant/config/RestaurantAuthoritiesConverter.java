package se.edugrade.java25.restaurant.config;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.DelegatingJwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

import java.util.Collection;

public class RestaurantAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final DelegatingJwtGrantedAuthoritiesConverter delegate;

    public RestaurantAuthoritiesConverter(String rolesClaim) {
        JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
        roles.setAuthoritiesClaimName(rolesClaim);
        roles.setAuthorityPrefix("ROLE_");
        delegate = new DelegatingJwtGrantedAuthoritiesConverter(
                new JwtGrantedAuthoritiesConverter(), roles);
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return delegate.convert(jwt);
    }
}

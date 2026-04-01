package fr.univrouen.driver.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@ConditionalOnProperty(name = "app.security.enabled", havingValue = "true", matchIfMissing = true)
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/health/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/drivers/**", "/licenses/**").hasAnyRole("admin", "utilisateur")
                        .requestMatchers(HttpMethod.POST, "/drivers/**", "/licenses/**").hasAnyRole("admin", "utilisateur")
                        .requestMatchers(HttpMethod.PUT, "/drivers/**").hasAnyRole("admin", "utilisateur")
                        .requestMatchers(HttpMethod.DELETE, "/drivers/**", "/licenses/**").hasAnyRole("admin", "utilisateur")
                        .requestMatchers("/graphql").hasAnyRole("admin", "utilisateur")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(this::toAuthentication)));

        return http.build();
    }

    private JwtAuthenticationToken toAuthentication(Jwt jwt) {
        Collection<GrantedAuthority> authorities = new ArrayList<>();

        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess != null) {
            Object rolesClaim = realmAccess.get("roles");
            if (rolesClaim instanceof Collection<?> roles) {
                for (Object roleObj : roles) {
                    if (roleObj instanceof String role && !role.isBlank()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.toLowerCase()));
                    }
                }
            }
        }

        return new JwtAuthenticationToken(jwt, authorities);
    }
}

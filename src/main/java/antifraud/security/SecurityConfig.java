package antifraud.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import java.security.SecureRandom;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Autowired
    public SecurityConfig(RestAuthenticationEntryPoint restAuthenticationEntryPoint) {
        this.restAuthenticationEntryPoint = restAuthenticationEntryPoint;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12, new SecureRandom());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        return http
                .httpBasic(c -> c.authenticationEntryPoint(restAuthenticationEntryPoint))
                .csrf(AbstractHttpConfigurer::disable)  // For modifying requests via Postman
                .exceptionHandling(handling -> handling
                        .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                )
                .headers(headers -> headers.frameOptions().disable())           // for Postman, the H2 console
                .authorizeHttpRequests(requests -> requests                     // manage access
                                // to prevent 401: (endpoints redirecting to the /error/** in case of error and /error/ is secured
                                // by spring security)
                                .requestMatchers("/error/**").permitAll()
                                .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                                .requestMatchers(HttpMethod.DELETE, "/api/auth/user/*")
                                .hasAuthority("ROLE_ADMINISTRATOR")
                                .requestMatchers(HttpMethod.GET, "/api/auth/list")
                                .hasAnyAuthority("ROLE_ADMINISTRATOR", "ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.PUT, "/api/auth/access")
                                .hasAuthority("ROLE_ADMINISTRATOR")
                                .requestMatchers(HttpMethod.PUT, "/api/auth/role")
                                .hasAuthority("ROLE_ADMINISTRATOR")
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction")
                                .hasAuthority("ROLE_MERCHANT")
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/*")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.POST, "/api/antifraud/stolencard")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/*")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/stolencard")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/history")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.GET, "/api/antifraud/history/*")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction")
                                .hasAuthority("ROLE_SUPPORT")
                                .requestMatchers("/actuator/shutdown").permitAll()      // needs to run test
                                .anyRequest().denyAll()
                        // other matchers
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                )
                // other configurations
                .build();
    }
}

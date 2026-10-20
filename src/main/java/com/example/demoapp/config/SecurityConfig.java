package com.example.demoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Klasa dostarczająca konfigurację kontenera Springa.
public class SecurityConfig {

    @Bean // Zwrócony SecurityFilterChain staje się obiektem zarządzanym przez Spring.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // 401 z JSON-em obsługuje formularz Angulara. Brak WWW-Authenticate zapobiega
        // drugiemu oknu logowania przeglądarki; API nadal wymaga poprawnego Basic Auth.
        AuthenticationEntryPoint apiLoginRequired = (request, response, exception) -> {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"message\":\"Nieprawidłowy login lub hasło.\"}");
        };
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Dostęp do Swaggera bez logowania
                        .anyRequest().authenticated()
                )
                // Frontend przesyła nagłówek Authorization; sam dostęp do Swaggera nie loguje do API.
                .httpBasic(basic -> basic.authenticationEntryPoint(apiLoginRequired))
                .exceptionHandling(errors -> errors.authenticationEntryPoint(apiLoginRequired));

        return http.build();
    }
}

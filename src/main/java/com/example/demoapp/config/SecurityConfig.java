package com.example.demoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration // Klasa dostarczająca konfigurację kontenera Springa.
public class SecurityConfig {

    @Bean // Zwrócony SecurityFilterChain staje się obiektem zarządzanym przez Spring.
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Dostęp do Swaggera bez logowania
                        .anyRequest().authenticated()
                )
                // Frontend przesyła nagłówek Authorization; sam dostęp do Swaggera nie loguje do API.
                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}

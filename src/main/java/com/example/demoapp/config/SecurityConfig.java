package com.example.demoapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Wyłączamy CSRF na potrzeby API
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated() // Wymagaj logowania dla każdego zapytania
                )
                .httpBasic(Customizer.withDefaults()); // Domyślne HTTP Basic (login: user + hasło z konsoli)

        return http.build();
    }
}
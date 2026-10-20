package com.example.demoapp.config;

import com.example.demoapp.repository.UserRepository;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.web.*;

@Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    AuthenticationEntryPoint unauthorized = (request, response, error) -> {
      response.setStatus(401);
      response.setContentType("application/json;charset=UTF-8");
      response.getWriter().write("{\"message\":\"Zaloguj się ponownie.\"}");
    };
    // JWT trafia w Authorization, nie w cookie. Serwer nie tworzy sesji HTTP.
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(s ->
        s.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      )
      .authorizeHttpRequests(auth ->
        auth
          .requestMatchers(
            "/api/auth/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/actuator/health"
          )
          .permitAll()
          .anyRequest()
          .authenticated()
      )
      .oauth2ResourceServer(oauth ->
        oauth.jwt(jwt -> {}).authenticationEntryPoint(unauthorized)
      )
      .exceptionHandling(errors ->
        errors.authenticationEntryPoint(unauthorized)
      );
    return http.build();
  }

  @Bean
  public UserDetailsService userDetailsService(UserRepository users) {
    return username -> {
      var user = users
        .findByUsernameIgnoreCase(username)
        .filter(u -> u.isEnabled())
        .orElseThrow(() ->
          new UsernameNotFoundException("Nieprawidłowe dane logowania")
        );
      return org.springframework.security.core.userdetails.User.withUsername(
        user.getUsername()
      )
        .password(user.getPassword())
        .roles(user.getRole().name())
        .build();
    };
  }
}

package com.example.demoapp.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.util.Base64;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;

@Configuration
public class JwtConfig {

  @Bean
  public SecretKey jwtKey(@Value("${app.jwt.secret}") String encoded) {
    byte[] bytes = Base64.getDecoder().decode(encoded);
    if (bytes.length < 32) throw new IllegalArgumentException(
      "JWT_SECRET wymaga przynajmniej 32 losowych bajtów w Base64"
    );
    return new SecretKeySpec(bytes, "HmacSHA256");
  }

  @Bean
  public JwtEncoder jwtEncoder(SecretKey key) {
    return new NimbusJwtEncoder(new ImmutableSecret<>(key));
  }

  @Bean
  public JwtDecoder jwtDecoder(SecretKey key) {
    var decoder = NimbusJwtDecoder.withSecretKey(key)
      .macAlgorithm(MacAlgorithm.HS256)
      .build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("dukat"));
    return decoder;
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
  }
}

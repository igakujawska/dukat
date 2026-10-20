package com.example.demoapp.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.*;
import org.springframework.context.annotation.*;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
      .info(
        new Info()
          .title("Dukat API")
          .version("2.0")
          .description(
            "Zaloguj się przez /api/auth/login, skopiuj accessToken i użyj Authorize. Każdy użytkownik widzi tylko własne zasoby."
          )
      )
      .components(
        new Components().addSecuritySchemes(
          "bearerAuth",
          new SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
        )
      )
      .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
  }
}

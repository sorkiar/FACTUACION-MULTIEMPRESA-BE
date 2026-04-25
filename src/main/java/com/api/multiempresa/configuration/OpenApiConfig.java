package com.api.multiempresa.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition
@Configuration
public class OpenApiConfig {
  @Value("${custom.server.hostname}")
  private String server;

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().components(new Components().addSecuritySchemes("BearerAuth",
            new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")))
        .addServersItem(new Server().url(server).description("Local Server"))
        .addSecurityItem(new SecurityRequirement().addList("BearerAuth"))
        .info(new Info().title("Multi-Empresa API Rest").version("1.0.0"));
  }
}
package com.rubensgomes.azure.acr.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import java.util.Objects;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Supplies the {@code info} section of the OpenAPI document that springdoc
 * generates.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties(OpenApiProperties.class)
public final class OpenApiConfig {

  private final OpenApiProperties properties;

  public OpenApiConfig(OpenApiProperties properties) {
    this.properties = Objects.requireNonNull(properties, "properties cannot be null");
  }

  /**
   * Builds the OpenAPI document metadata from the configured properties.
   *
   * @return the OpenAPI document carrying only its {@code info} section
   */
  @Bean
  public OpenAPI openApi() {
    Info info =
        new Info()
            .title(properties.title())
            .description(properties.description())
            .version(properties.version());
    return new OpenAPI().info(info);
  }
}

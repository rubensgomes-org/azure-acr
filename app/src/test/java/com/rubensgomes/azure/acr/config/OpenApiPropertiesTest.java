package com.rubensgomes.azure.acr.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySource;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;

/**
 * Verifies that {@link OpenApiProperties} binds from the {@code openapi}
 * prefix.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
class OpenApiPropertiesTest {

  private static final String DESCRIPTION =
      "Spring Boot demo that builds and publishes its container image to Azure Container Registry";

  @Test
  @DisplayName("the openapi prefix binds onto the record components")
  void bindsFromTheOpenapiPrefix() {
    ConfigurationPropertySource source =
        new MapConfigurationPropertySource(
            Map.of(
                "openapi.title", "Azure Container Registry Demo",
                "openapi.description", DESCRIPTION,
                "openapi.version", "0.0.9-SNAPSHOT"));

    OpenApiProperties properties =
        new Binder(source).bind("openapi", OpenApiProperties.class).get();

    assertThat(properties.title()).isEqualTo("Azure Container Registry Demo");
    assertThat(properties.description()).isEqualTo(DESCRIPTION);
    assertThat(properties.version()).isEqualTo("0.0.9-SNAPSHOT");
  }
}

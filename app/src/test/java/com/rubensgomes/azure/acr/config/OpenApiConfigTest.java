package com.rubensgomes.azure.acr.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import io.swagger.v3.oas.models.OpenAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link OpenApiConfig}.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
class OpenApiConfigTest {

  private static final OpenApiProperties PROPERTIES =
      new OpenApiProperties("Azure Container Registry Demo", "A demo service", "1.2.3");

  @Test
  @DisplayName("openApi copies every configured property into the info section")
  void openApiCarriesTheConfiguredMetadata() {
    OpenAPI openApi = new OpenApiConfig(PROPERTIES).openApi();

    assertThat(openApi.getInfo()).isNotNull();
    assertThat(openApi.getInfo().getTitle()).isEqualTo("Azure Container Registry Demo");
    assertThat(openApi.getInfo().getDescription()).isEqualTo("A demo service");
    assertThat(openApi.getInfo().getVersion()).isEqualTo("1.2.3");
  }

  @Test
  @DisplayName("openApi contributes metadata only, leaving paths to springdoc")
  void openApiDeclaresNoPaths() {
    assertThat(new OpenApiConfig(PROPERTIES).openApi().getPaths()).isNull();
  }

  @Nested
  @DisplayName("construction")
  class Construction {

    @Test
    @DisplayName("null properties are rejected rather than deferred to bean creation")
    void nullPropertiesAreRejected() {
      assertThatNullPointerException()
          .isThrownBy(() -> new OpenApiConfig(null))
          .withMessage("properties cannot be null");
    }
  }
}

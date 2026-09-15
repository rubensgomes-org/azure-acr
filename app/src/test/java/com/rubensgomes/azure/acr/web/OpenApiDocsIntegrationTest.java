package com.rubensgomes.azure.acr.web;

import static org.assertj.core.api.Assertions.assertThat;

import com.rubensgomes.azure.acr.web.controller.HelloWorldRestController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClient;

/**
 * Verifies that springdoc is wired into a running container and that the
 * document it generates describes the API this service actually exposes.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "openapi.title=Azure Container Registry Demo",
      "openapi.description=Spring Boot demo that builds and publishes its container image to Azure"
          + " Container Registry",
      "openapi.version=0.0.9-SNAPSHOT",
      "springdoc.api-docs.enabled=true",
      "springdoc.swagger-ui.enabled=true",
      "springdoc.paths-to-exclude=/error"
    })
class OpenApiDocsIntegrationTest {

  private static final String API_DOCS_PATH = "/v3/api-docs";

  @Value("${local.server.port}")
  private int port;

  private RestClient client;

  @BeforeEach
  void setUp() {
    client = RestClient.create("http://localhost:" + port);
  }

  private ResponseEntity<String> get(String path) {
    return client.get().uri(path).retrieve().toEntity(String.class);
  }

  @Test
  @DisplayName("the generated document carries the configured info section")
  void apiDocsDescribeTheService() {
    ResponseEntity<String> response = get(API_DOCS_PATH);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody())
        .contains("\"title\":\"Azure Container Registry Demo\"")
        .contains("\"version\":\"0.0.9-SNAPSHOT\"");
  }

  @Test
  @DisplayName("the greeting endpoint and its annotations reach the document")
  void apiDocsDescribeTheGreetingOperation() {
    assertThat(get(API_DOCS_PATH).getBody())
        .contains(HelloWorldRestController.HELLO_WORLD_OPERATION_PATH)
        .contains("Hello World")
        .contains("Return a greeting")
        .contains("MessageResponse");
  }

  @Test
  @DisplayName("the container-internal /error forward target is excluded")
  void apiDocsOmitTheErrorPath() {
    assertThat(get(API_DOCS_PATH).getBody()).doesNotContain("\"/error\"");
  }

  @Nested
  @DisplayName("Swagger UI")
  class SwaggerUi {

    @Test
    @DisplayName("the console is served from the webjar")
    void swaggerUiIsServed() {
      assertThat(get("/swagger-ui/index.html").getStatusCode()).isEqualTo(HttpStatus.OK);
    }
  }
}

package com.rubensgomes.azure.acr;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;

/**
 * Unit tests for the {@link App} entry point.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
class AppMainTest {

  @Test
  @DisplayName("main boots the Spring application with the App class and the given arguments")
  void mainDelegatesToSpringApplication() {
    String[] args = {"--server.port=0"};

    try (MockedStatic<SpringApplication> springApplication =
        Mockito.mockStatic(SpringApplication.class)) {
      App.main(args);

      springApplication.verify(() -> SpringApplication.run(App.class, args));
      springApplication.verifyNoMoreInteractions();
    }
  }

  @Test
  @DisplayName("main tolerates an empty argument array")
  void mainAcceptsNoArguments() {
    String[] args = {};

    try (MockedStatic<SpringApplication> springApplication =
        Mockito.mockStatic(SpringApplication.class)) {
      App.main(args);

      springApplication.verify(() -> SpringApplication.run(App.class, args));
    }
  }

  @Test
  @DisplayName("the application class is instantiable by the container")
  void appIsInstantiable() {
    assertThat(new App()).isNotNull();
  }
}

package com.rubensgomes.azure.acr.web.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.rubensgomes.azure.acr.model.response.MessageResponse;
import com.rubensgomes.azure.acr.service.HelloWorldService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

/**
 * Unit tests for {@link HelloWorldRestController}.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@ExtendWith(MockitoExtension.class)
class HelloWorldRestControllerTest {

  private static final MessageResponse GREETING = new MessageResponse("Hello World!");

  @Mock private HelloWorldService service;

  private HelloWorldRestController controller;

  @BeforeEach
  void setUp() {
    controller = new HelloWorldRestController(service);
  }

  @Test
  @DisplayName("helloWorld answers 200 OK carrying the response built by the service")
  void helloWorldReturnsTheServiceResponse() {
    when(service.helloWorld()).thenReturn(GREETING);

    ResponseEntity<MessageResponse> response = controller.helloWorld();

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isSameAs(GREETING);
    verify(service).helloWorld();
    verifyNoMoreInteractions(service);
  }

  @Test
  @DisplayName("the controller does not rewrite what the service returns")
  void helloWorldDelegatesWithoutAlteringTheMessage() {
    when(service.helloWorld()).thenReturn(new MessageResponse("anything at all"));

    assertThat(controller.helloWorld().getBody()).isEqualTo(new MessageResponse("anything at all"));
  }

  @Nested
  @DisplayName("HTTP layer")
  class HttpLayer {

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
      mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("GET /api/v1/helloworld returns the greeting as JSON")
    void getHelloWorldReturnsJson() throws Exception {
      when(service.helloWorld()).thenReturn(GREETING);

      mockMvc
          .perform(get(HelloWorldRestController.HELLO_WORLD_OPERATION_PATH))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message").value("Hello World!"));

      verify(service).helloWorld();
    }

    @Test
    @DisplayName("an unmapped path is not served by this controller")
    void anUnknownPathIsNotFound() throws Exception {
      mockMvc.perform(get("/api/v1/unknown")).andExpect(status().isNotFound());

      verifyNoMoreInteractions(service);
    }
  }
}

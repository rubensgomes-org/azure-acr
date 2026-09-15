package com.rubensgomes.azure.acr.web.controller;

import com.rubensgomes.azure.acr.model.response.MessageResponse;
import com.rubensgomes.azure.acr.service.HelloWorldService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A very simple {@link RestController} that responds with a "Hello World!"
 * message.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Slf4j
@RestController
@Tag(name = "Hello World", description = "Minimal greeting endpoint used to prove the deployment.")
public class HelloWorldRestController {

  /** Constant becomes handy in unit testing. */
  public static final String HELLO_WORLD_OPERATION_PATH = "/api/v1/helloworld";

  private final HelloWorldService service;

  public HelloWorldRestController(HelloWorldService service) {
    this.service = service;
  }

  @Operation(
      summary = "Return a greeting",
      description =
          "Takes no input and always succeeds. Serves as an end-to-end check that the service is"
              + " reachable and answering with JSON.")
  @ApiResponse(
      responseCode = "200",
      description = "The greeting was produced.",
      content =
          @Content(
              mediaType = MediaType.APPLICATION_JSON_VALUE,
              schema = @Schema(implementation = MessageResponse.class)))
  @GetMapping(path = HELLO_WORLD_OPERATION_PATH, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<MessageResponse> helloWorld() {
    log.trace("helloWorld()");
    MessageResponse response = service.helloWorld();
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}

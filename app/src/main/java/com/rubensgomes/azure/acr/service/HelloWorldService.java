package com.rubensgomes.azure.acr.service;

import com.rubensgomes.azure.acr.model.response.MessageResponse;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * A very simple service class that responds with a "Hello World!" message
 * response.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Slf4j
@Service
public class HelloWorldService {

  public MessageResponse helloWorld() {
    log.trace("helloWorld()");
    // this is where business domain layer would be called from.
    return new MessageResponse("Hello World!");
  }

  @PreDestroy
  public void cleanup() {
    log.info("I am being terminated.");
  }
}

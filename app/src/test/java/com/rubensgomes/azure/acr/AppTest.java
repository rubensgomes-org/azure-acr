package com.rubensgomes.azure.acr;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Verifies that the Spring application context wires up.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@SpringBootTest
class AppTest {

  @Autowired private ApplicationContext context;

  @Test
  void contextLoads() {
    assertNotNull(context, "application context should have been loaded");
  }
}

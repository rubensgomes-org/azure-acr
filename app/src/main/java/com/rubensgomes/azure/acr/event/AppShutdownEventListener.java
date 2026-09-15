package com.rubensgomes.azure.acr.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

/**
 * Handles application shutdown to properly release resources.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Slf4j
@Component
public class AppShutdownEventListener implements ApplicationListener<ContextClosedEvent> {

  @Override
  public void onApplicationEvent(ContextClosedEvent event) {
    log.info("Handling SIGTERM");
  }
}

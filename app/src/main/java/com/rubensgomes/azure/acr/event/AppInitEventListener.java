package com.rubensgomes.azure.acr.event;

import java.net.InetAddress;
import java.net.UnknownHostException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.server.servlet.context.ServletWebServerInitializedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Handles application initialization event to display IP and port.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Slf4j
@Component
public class AppInitEventListener implements ApplicationListener<ServletWebServerInitializedEvent> {

  @Override
  public void onApplicationEvent(ServletWebServerInitializedEvent event) {
    log.info("application started");
    int port = event.getWebServer().getPort();

    try {
      InetAddress address = InetAddress.getLocalHost();
      String ip = address.getHostAddress();
      log.info("IP address {}", ip);
    } catch (UnknownHostException ex) {
      log.warn("failed to resolve the local host IP address: {}", ex.getMessage());
    }

    log.info("Listening port {}", port);
  }
}

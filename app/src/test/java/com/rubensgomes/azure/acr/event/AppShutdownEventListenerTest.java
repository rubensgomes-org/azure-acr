package com.rubensgomes.azure.acr.event;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.event.ContextClosedEvent;

/**
 * Unit tests for {@link AppShutdownEventListener}.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@ExtendWith(MockitoExtension.class)
class AppShutdownEventListenerTest {

  @Mock private ContextClosedEvent event;

  @Test
  @DisplayName("the shutdown listener handles the context closed event quietly")
  void onApplicationEventHandlesTheEvent() {
    AppShutdownEventListener listener = new AppShutdownEventListener();

    assertThatCode(() -> listener.onApplicationEvent(event)).doesNotThrowAnyException();

    // the listener only logs; it must not touch the event it is handed.
    verifyNoInteractions(event);
  }
}

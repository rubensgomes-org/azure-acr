package com.rubensgomes.azure.acr.web.controller;

import com.rubensgomes.azure.acr.model.response.ErrorResponse;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.webmvc.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * Renders every failed request as JSON instead of Spring Boot's Whitelabel HTML
 * page.
 *
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Slf4j
@RestController
public class GlobalErrorController implements ErrorController {

  /** Constant becomes handy in unit testing. */
  public static final String ERROR_PATH = "/error";

  /** Stands in for the request URI when the container did not record one. */
  static final String UNKNOWN_PATH = "unknown";

  /** Stands in for the detail message when the container did not record one. */
  static final String NO_DETAIL_MESSAGE = "no further detail available";

  /**
   * Builds the JSON error body from the request attributes the container
   * populated before forwarding here.
   *
   * <p>Every method but {@code TRACE} is mapped because the container forwards
   * the original request method: narrowing this to {@code GET} would turn every
   * non-GET failure into a {@code 405} rather than the status it actually
   * caused. {@code TRACE} is omitted because echoing a request back is a
   * cross-site tracing liability.
   *
   * @param request the forwarded request carrying the
   *     {@code jakarta.servlet.error.*} attributes
   * @return the error body, with the same HTTP status the original request
   *     failed with
   */
  @RequestMapping(
      path = ERROR_PATH,
      method = {
        RequestMethod.GET,
        RequestMethod.HEAD,
        RequestMethod.POST,
        RequestMethod.PUT,
        RequestMethod.PATCH,
        RequestMethod.DELETE,
        RequestMethod.OPTIONS,
      },
      produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ErrorResponse> handleError(HttpServletRequest request) {
    Objects.requireNonNull(request, "request must not be null");

    HttpStatus status = resolveStatus(request);
    String path = attribute(request, RequestDispatcher.ERROR_REQUEST_URI, UNKNOWN_PATH);
    String message = attribute(request, RequestDispatcher.ERROR_MESSAGE, NO_DETAIL_MESSAGE);
    log.warn("request to '{}' failed with status {}: {}", path, status.value(), message);

    ErrorResponse body =
        new ErrorResponse(Instant.now(), status.value(), status.getReasonPhrase(), message, path);
    return new ResponseEntity<>(body, status);
  }

  private static HttpStatus resolveStatus(HttpServletRequest request) {
    return Optional.ofNullable(request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE))
        .filter(Integer.class::isInstance)
        .map(Integer.class::cast)
        .map(HttpStatus::resolve)
        .orElse(HttpStatus.INTERNAL_SERVER_ERROR);
  }

  private static String attribute(HttpServletRequest request, String name, String fallback) {
    return Optional.ofNullable(request.getAttribute(name))
        .map(Object::toString)
        .filter(value -> !value.isBlank())
        .orElse(fallback);
  }
}

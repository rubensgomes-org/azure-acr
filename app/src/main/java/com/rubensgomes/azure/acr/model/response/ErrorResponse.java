package com.rubensgomes.azure.acr.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.Instant;

/**
 * The JSON body returned for any failed request.
 *
 * @param timestamp when the failure was rendered
 * @param status the HTTP status code, for example {@code 404}
 * @param error the HTTP reason phrase, for example {@code Not Found}
 * @param message detail about the failure, or a placeholder when the container
 *     supplied none
 * @param path the request URI that failed
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Schema(description = "The JSON body returned for any failed request.")
public record ErrorResponse(
    @Schema(description = "When the failure was rendered.", example = "2026-09-08T12:00:00Z")
        Instant timestamp,
    @Schema(description = "The HTTP status code.", example = "404") int status,
    @Schema(description = "The HTTP reason phrase.", example = "Not Found") String error,
    @Schema(description = "Detail about the failure.", example = "No message available")
        String message,
    @Schema(description = "The request URI that failed.", example = "/api/v1/missing")
        String path) {}

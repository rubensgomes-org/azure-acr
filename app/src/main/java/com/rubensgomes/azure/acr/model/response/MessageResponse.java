package com.rubensgomes.azure.acr.model.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * A very basic message response type.
 *
 * @param message any text to be in the response
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@Schema(description = "A single text message returned by the service.")
public record MessageResponse(
    @Schema(description = "The message text.", example = "Hello World!")
        @Valid
        @NotBlank(message = "message cannot be blank")
        String message) {}

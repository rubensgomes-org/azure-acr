package com.rubensgomes.azure.acr.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Metadata describing the generated OpenAPI document.
 *
 * @param title the human readable name of the API
 * @param description a short summary of what the API offers
 * @param version the version of the API, tracking the project version
 * @author Rubens Gomes
 * @implNote This project's source code and documentation were generated
 *     with the assistance of Artificial Intelligence (AI). For more
 *     information, please refer to the `AI_DISCLAIMER.md` document located
 *     in the project's root directory.
 */
@ConfigurationProperties(prefix = "openapi")
public record OpenApiProperties(String title, String description, String version) {}

# azure-acr

[![Java](https://img.shields.io/badge/Java-JDK%2025-0969da?logo=java)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1%2B-0969da?logo=spring)](https://spring.io/projects/spring-boot#overview)
[![AI Assisted](https://img.shields.io/badge/AI%20Assisted-Development-d29922)](https://github.com/rubensgomes-org/azure-acr/blob/main/AI_DISCLAIMER.md)
[![License](https://img.shields.io/badge/License-MIT-0969da)](https://github.com/rubensgomes-org/azure-acr/blob/main/LICENSE)

Spring Boot demo that builds and publishes its container image to Azure
Container Registry.

---

## AI Disclaimer

This project includes code and documentation created with the assistance of AI
tools. For details on usage, limits, and review practices, please see
the [AI_DISCLAIMER](./AI_DISCLAIMER.md).

## Installation

To use this project, ensure that your environment is properly configured and
that the required tools are installed.

## Prerequisites

The following prerequisites are required:

- Microsoft Azure account
- An active Azure subscription
- An Azure RBAC role that allows you to create the resources, such as resource
  groups, container registry, container apps, and databases.
- GitHub account
- Azure CLI 2.90+
- GitHub CLI (`gh`) 2.99+
- Git 2.55+
- GNU Make 3.8+
- gradle 9.7.1+
- java 25+
- Spring Boot 4.1+
- Docker Desktop 4.87+

### Configuration

Follow the instructions in [INITIAL_SETUP](docs/INITIAL_SETUP.md).

## GitHub Actions

| Workflow               | Purpose                                                                                 |
|------------------------|-----------------------------------------------------------------------------------------|
| `build-verify.yml`     | Compiles, tests, checks and assembles; optionally blocks on the SonarCloud quality gate |
| `release.yml`          | Cuts a release: commits, tags, pushes, bumps, and publishes a GitHub Release            |
| `build-deploy.yml`     | Builds the container image and pushes it to an existing Azure Container Registry        |
| `repo-delete.yml`      | **Destructive.** Deletes an entire repository from an Azure Container Registry          |
| `acr-create.yml`       | Provisions the environment's Azure Container Registry via `azure-iac`                   |
| `acr-destroy.yml`      | **Destructive.** Destroys the environment's Azure Container Registry via `azure-iac`    |

## Development Workflow

See [DEVELOPMENT_WORKFLOW](./docs/DEVELOPMENT_WORKFLOW.md) for guidance on
developing, and cutting a release on this project.

## License

The project is licensed under
[MIT License](https://github.com/rubensgomes-org/azure-acr/blob/main/LICENSE).

## Links

- [GitHub Project](https://github.com/rubensgomes-org/azure-acr)
- [Azure Container Registry](https://github.com/rubensgomes-org/azure-acr/blob/main/docs/ACR.md)
- [Development Workflow](https://github.com/rubensgomes-org/azure-acr/blob/main/docs/DEVELOPMENT_WORKFLOW.md)
- [Initial Setup](https://github.com/rubensgomes-org/azure-acr/blob/main/docs/INITIAL_SETUP.md)


---
Author:  [Rubens Gomes](https://rubensgomes.com/)

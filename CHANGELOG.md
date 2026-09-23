# Changelog

All notable changes to this project are documented in this file.

Format: [Keep a Changelog](https://keepachangelog.com/en/1.1.0/).
Versioning: [Semantic Versioning](https://semver.org/).

## How this file is used

`.github/workflows/release.yml` extracts the section matching the version being
released and uses it verbatim as the GitHub Release notes. An empty or missing
section fails the run, so the section must exist **before** the release is
cut.

The `net.researchgate.release` plugin commits the release version, tags *that*
commit, then bumps to the next snapshot. So the order is:

1. Write what changed under `[Unreleased]`, commit, and push to `main`.
2. Run the `release` workflow.

Its `plan` job renames `[Unreleased]` to the version being released, adds a
fresh empty `[Unreleased]` above it, and pushes that change to `main` itself.
It rejects an `[Unreleased]` section that still holds only the empty
`### Added` / `### Changed` / `### Fixed` skeleton, since there is nothing to
rename.

## [Unreleased]

### Added

- `.github/dependabot.yml`: daily Dependabot updates for Gradle, GitHub
  Actions, Docker, and Docker Compose dependencies.

### Changed

### Fixed

## [0.0.10] - 2026-09-22

### Added

- `scripts/initvars.sh` now sets the `TF_VAR_*` Action variables the
  `acr-create` and `acr-destroy` workflows require.
- Dependabot daily updates for Gradle, GitHub Actions, and Docker.

### Changed

### Fixed

- `build-deploy` now calls the renamed `acr-build-push-java` reusable
  workflow; `acr-build-deploy-java` no longer exists at `@v0`.

## [0.0.9] - 2026-09-22

### Added

- `acr-create` and `acr-destroy` workflows that provision and destroy the
  environment's registry via `azure-iac`.

### Changed

- **Breaking:** `build-deploy` and `repo-delete` no longer take a
  `registry_name` input; the registry is derived from `environment`
  (`dev` → `crrgomesdev01`, `lab` → `crrgomeslab02`). `build-deploy`'s
  `environment` is now a `dev`/`lab` dropdown.

### Fixed

## [0.0.8] - 2026-09-21

### Added

### Changed

- Renamed `acr-repo-delete.yml` workflow to `repo-delete.yml`.
- Renamed `acr-build-deploy.yml` workflow to `build-deploy.yml`.

### Fixed

## [0.0.7] - 2026-09-18

### Added

- `release.yml`'s `plan` job now renames `[Unreleased]` to the release
  version and commits a fresh empty `[Unreleased]` to `main` itself, so
  that step no longer needs to be done manually before running the
  workflow.

### Changed

- `acr-build-deploy.yml` now calls the renamed `acr-build-deploy-java`
  reusable workflow and passes `artifact-id: azure-acr`, `java-version`, and
  `java-distribution` inputs it requires.

### Fixed

## [0.0.6] - 2026-09-17

### Added

- `acr-repo-delete.yml` now requires a `confirm` checkbox input, gating the
  `delete` job, before it will run.

### Changed

- `acr-repo-delete.yml` now passes `artifact-id: azure-acr` to the reusable
  `acr-repo-delete` workflow, which requires it as an input instead of
  reading `app/gradle.properties`.

### Fixed

## [0.0.5] - 2026-09-17

### Added

- `scripts/initvars.sh` to reset this repository's Actions variables
  (`AZURE_CLIENT_ID`, `AZURE_SUBSCRIPTION_ID`, `AZURE_TENANT_ID`) and secrets
  (`RUBENS_PAT_TOKEN`, `AZURE_CLIENT_SECRET`, `SONAR_TOKEN`) from the current
  shell environment.
- Document `SONAR_TOKEN` in `docs/INITIAL_SETUP.md`.

## [0.0.4] - 2026-09-15

### Added

- `build-verify.yml` now exposes a `run-sonar` `workflow_dispatch` input
  (checkbox), so the SonarCloud gate can be toggled from the "Run workflow"
  UI instead of being hardcoded in the workflow file.

## [0.0.3] - 2026-09-15

### Changed

- **Breaking:** `environment` and `registry_name` are now required dropdown
  (`choice`) inputs on `acr-build-deploy` and `acr-repo-delete`, restricted to
  `dev`/`lab` and `crrgomesdev01`/`crrgomeslab02` respectively, instead of
  optional free-text strings.

## [0.0.2] - 2026-09-15

### Changed

- **Breaking:** the `acr-repo-delete` confirm phrase is now
  `DELETE REPO <registry_name> <environment>/<artifactId>`, so a mistyped
  registry cannot pass the safeguard.
- `acr-repo-delete` and `scripts/acr-repo-delete.sh` now succeed when the
  registry does not exist, matching their existing behavior for a repository
  that does not exist.

## [0.0.1] - 2026-09-15

### Changed

- Point the SCM coordinates and the image `org.opencontainers.image.source`
  label at the new `azure-acr` repository location.
- Target the `crrgomesdev01` registry instead of `crrgomeslab01` in the
  `acr-build-deploy` and `acr-repo-delete` workflows, the `acr-repo-delete`
  script, and `docs/ACR.md`.
- Default the image namespace to `dev` instead of `lab`.
- Refer to the sibling repositories as `azure-iac` and `azure-workflows`.

## [0.0.0] - 2026-09-14

### Added

- Spring Boot REST service exposing a hello-world endpoint, a global error
  handler, and OpenAPI documentation.
- `Dockerfile` and `docker-compose.yml` for building and running the service
  as a container locally.
- GitHub Actions workflows: `build-verify`, `release`, `acr-build-deploy`, and
  `acr-repo-delete`, targeting the `crrgomesdev01` registry.
- Gradle build with dependency locking, Maven publishing, the
  `net.researchgate.release` flow, and the SonarCloud quality gate.
- Documentation: `INITIAL_SETUP`, `DEVELOPMENT_WORKFLOW`, and `ACR`.

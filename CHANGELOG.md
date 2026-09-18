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

1. Rename `[Unreleased]` to the version being released, and add a fresh empty
   `[Unreleased]` above it.
2. Write what changed under it.
3. Commit and push that change to `main`.
4. Run the `release` workflow.

The workflow's `plan` job enforces this before anything is written, so getting
it wrong costs a failed run and nothing else. It rejects both a missing section
and one that still holds only the empty `### Added` / `### Changed` /
`### Fixed` skeleton.

## [Unreleased]

### Added

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

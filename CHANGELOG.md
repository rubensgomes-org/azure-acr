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

### Fixed

## [0.0.0] - 2026-09-14

### Added

- Spring Boot REST service exposing a hello-world endpoint, a global error
  handler, and OpenAPI documentation.
- `Dockerfile` and `docker-compose.yml` for building and running the service
  as a container locally.
- GitHub Actions workflows: `build-verify`, `release`, `acr-build-deploy`, and
  `acr-repo-delete`.
- Gradle build with dependency locking, Maven publishing, the
  `net.researchgate.release` flow, and the SonarCloud quality gate.
- Documentation: `INITIAL_SETUP`, `DEVELOPMENT_WORKFLOW`, and `ACR`.

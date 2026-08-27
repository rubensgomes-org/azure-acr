# Extract the Workflow Setup and Build Steps into Composite Actions

## Goal

Stop restating the Java 25 / Microsoft toolchain pin, the `GRADLE_ARGS` flags
and the `GITHUB_USER`/`GITHUB_TOKEN` credential dance in every workflow. Move
them into local composite actions under `.github/actions/`.

Source: `build-verify.yml` lines 161-244 as they stood at commit `88dd346`.

## What shipped

Two actions, not one — split so a workflow can take the toolchain without the
build:

| Action              | Inputs                                          | Steps                              |
|---------------------|-------------------------------------------------|------------------------------------|
| `setup-java-gradle` | `java-version`, `java-distribution`             | setup-java, setup-gradle           |
| `build`             | `packages-user`, `packages-token`, `gradle-args` | compile, test, check, assemble     |

Consumers:

- `build-verify.yml` — both actions, then `sonar` itself.
- `release.yml` — `setup-java-gradle` only; `./gradlew release` runs the build
  itself via `runBuildTasks`.
- `acr-build-deploy.yml` — both actions, to produce the jar the Dockerfile
  copies.

## Tasks

- [x] Create the two actions under `.github/actions/`.
- [x] Rewire `build-verify.yml` to call both; keep `sonar` in the workflow.
- [x] Rewire `release.yml` to call `setup-java-gradle`.
- [x] Add the `assemble` step (`:app:build`) to the `build` action.
- [x] Update `README.md`, `BUILD.md` and `llms.txt`.

## Design decisions

| Decision | Reason |
|----------|--------|
| Credentials are inputs, not `secrets` reads | A composite action cannot access the `secrets` context. |
| `export GITHUB_USER=…` stays inside the `run:` body | Actions reserves the `GITHUB_` prefix in any `env:` block, and exporting from a carrier keeps the token off the command line. |
| Env set per step inside the action | An action must not assume anything about the workflow invoking it. |
| `shell: bash` on every `run:` | Required for composite actions, and load-bearing: `$GRADLE_ARGS` is expanded unquoted and needs bash word-splitting. |
| Task lists are not inputs | The fixed compile/test/check/assemble sequence is the point of the action. Workflows needing other tasks call `./gradlew` directly, as `release.yml` does. |
| Neither action checks out | Callers differ (full history; `ref: main` + PAT; plain), and a local `uses: ./…` action is read from the workspace, so checkout must come first anyway. |
| `build` ends with `:app:build`, not `:app:check` | `applicationJar` and `generateDotEnv` hang off `tasks.build` (`app/build.gradle.kts:758`). `:app:check` reaches neither — verified with `--dry-run`. Without it there is no `-spring-boot.jar` and no `.env`. |

## Verification

- [x] All action and workflow YAML parses; every `run:` block passes `bash -n`.
- [x] Every `uses: ./…` resolves to a real `action.yml`.
- [x] The rendered Gradle command sequence is unchanged from `88dd346`.
- [x] `./gradlew :app:build --dry-run` reaches `applicationJar` and
      `generateDotEnv`; `:app:check --dry-run` reaches neither.
- [ ] Push to `main` and confirm the run is green with the phases as separate
      red/green steps.

## Review

`build-verify.yml` went from 275 to ~150 lines and from six steps to four.
Nothing about what runs changed except the added `assemble` step — same actions
at the same versions, same Gradle commands in the same order, same credential
handling.

The remaining duplication is `GRADLE_ARGS`, still declared in each workflow's
job `env`. `release.yml` cannot take it from the `build` action because it does
not call that action. That is inherent to the split.

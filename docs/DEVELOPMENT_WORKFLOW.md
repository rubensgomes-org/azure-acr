## Working on This Project

There are three primary workflows: making a modification, shipping a change and
cutting a release. All work occurs directly on the `main` branch. This
repository follows a trunk-based development model, with no feature branches or
pull requests.

The decision to use a single branch was made to keep the development and
maintenance of the project simple. This approach is appropriate because the
project was originally created by [Rubens Gomes](https://rubensgomes.com) to be
maintained by a single person.

### Starting New Work

Before starting new work, ensure that all required prerequisites are installed.

1. Sync the local `main` branch with the remote repository.

    ```bash
    # Always start from a current main.
    git switch main && git pull
    ```

2. Modify the project as needed.

3. Create a new entry [X.Y.Z] in `CHANGELOG.md` for the next
   `app/gradle. properties` version, minus -SNAPSHOT. Update the corresponding
   sections "Added", "Changed", and "Fixed" as needed.

**Write the `CHANGELOG.md` entry as you go.** `release.yml` refuses to cut a
release on a missing `[X.Y.Z]` for the upcoming release.

4. Add, commit changes.

   ```bash
   git add -A && git commit
   ```

### Cutting a Release

1. Ensure `CHANGELOG.md` is updated.

2. Push all the changes.

   ```bash
   git push
   ```

3. Simply run the following workflow to cut a release

    ```bash
    gh workflow run release.yml
    ```

4. The workflow runs three jobs

| Job       | What it does                                                   |
|-----------|----------------------------------------------------------------|
| `plan`    | Resolves the version and tag, and checks `CHANGELOG.md`        |
| `cut`     | `./gradlew release` — strips `-SNAPSHOT`, commits, tags, bumps |
| `publish` | Creates the GitHub Release from that CHANGELOG section         |

### Do NOT release from a workstation

`./gradlew release` still works locally, but it is now only the middle third of
a release: it skips the `CHANGELOG.md` gate and never creates the GitHub
Release. The result is a pushed tag with no Release entry. Nothing watches
for the tag, so the Release has to be created by hand afterward.

### Versioning

The build uses locked dependency versions. Any catalog change that moves a
library or framework version requires regenerating the lock files.

- Regenerate the version lock files

    ```bash
    ./gradlew :app:dependencies --write-locks
    ```

### Common commands

| Command                                              | Purpose                                              |
|------------------------------------------------------|------------------------------------------------------|
| `./gradlew bootRun`                                  | Run locally on port 8080                             |
| `./gradlew test`                                     | Run the suite; coverage report always follows        |
| `./gradlew build`                                    | Format check + tests + coverage gate + all artifacts |
| `./gradlew spotlessApply`                            | Reformat sources                                     |
| `./gradlew publishToMavenLocal`                      | Install to `~/.m2`                                   |
| `./gradlew release`                                  | Tag and bump only — no Release; use the workflow     |
| `./gradlew :app:dependencies --write-locks`          | Regenerate the `:app` dependency lock files          |
| `gh workflow run build-verify.yml -f run-sonar=true` | Run the build in CI, with the Sonar gate             |
| `gh workflow run release.yml`                        | Cut a release: check changelog, tag, publish Release |
| `gh workflow run build-deploy.yml`                   | Build and push the image to ACR                      |
| `docker compose up --build -d`                       | Build and run the image — `./gradlew build` first    |
| `docker compose down`                                | Stop and remove the container                        |

## Quick start

### Option 1 — Gradle

No JDK setup required: Gradle downloads the Java 25 Microsoft Build of OpenJDK
toolchain on first build. DevTools is active, so edits to `src/main` restart the
app automatically. Activates the `local` profile.

- Build and run application using `gradlew`

    ```bash
    ./gradlew bootRun
    ```

### Option 2 — Docker

Run `./gradlew build` first. It packages the jar the Dockerfile copies and
regenerates the root `.env` holding `APP_VERSION`, which is read by
`docker compose up --build -d` below.

- Build application jar, docker image and launch container

    ```bash
    ./gradlew build
    docker compose up --build -d      # build and start
    docker compose logs -f app        # follow the logs
    docker compose down               # stop and remove
    ```

### Verify either one

- Ensure the application responds using browser

    ```bash
    curl http://localhost:8080/api/v1/helloworld
    # {"message":"Hello World!"}
    
    curl http://localhost:8080/actuator/health
    # {"status":"UP"}
    ```

### Stopping it

**Gradle** — <kbd>Ctrl</kbd>+<kbd>C</kbd> in the `bootRun` terminal. Shutdown is
graceful, but its logs are discarded and Gradle then prints `BUILD FAILED`. Both
are expected.

**Docker** — `docker compose down`. SIGTERM reaches the JVM as PID 1, so the
same graceful shutdown applies.

## REST API

| Endpoint                 | Purpose                 |
|--------------------------|-------------------------|
| `GET /api/v1/helloworld` | Returns a JSON greeting |
| `GET /actuator/health`   | Liveness of the service |

### API documentation

`./gradlew bootRun` activates the `local` profile, which serves an OpenAPI 3
document generated from the controllers and a Swagger UI console for browsing
and exercising the API:

- Swagger UI — <http://localhost:8080/swagger-ui.html>
- OpenAPI document — <http://localhost:8080/v3/api-docs>

Both are **disabled in every other profile**, so a deployed instance of this
image serves neither. This project carries no Spring Security, so an enabled
console would be an open, unauthenticated way to browse and drive the API. To
turn them on elsewhere, set `springdoc.api-docs.enabled` and
`springdoc.swagger-ui.enabled` to `true` for that profile, and add
authentication first.

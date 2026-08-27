# azure-acr

Spring Boot demo that builds and publishes its container image to Azure
Container Registry.

This is a **scaffold**. The build, CI, release, and container machinery are
complete and green; the Azure Container Registry integration itself is not wired
up yet — see
[misc/tasks/scaffold-todo.md](misc/tasks/scaffold-todo.md). The placeholder
`GET /api/v1/helloworld` endpoint stands in until a real one replaces it.

## Quick start

```bash
git clone https://github.com/rubensgomes-org/azure-acr.git
cd azure-acr
```

Export your GitHub Packages credentials first — dependencies, including the
shared version catalog, resolve from a private repository:

```bash
export GITHUB_USER=<your-github-username>
export GITHUB_TOKEN=<a-PAT-with-read:packages>
```

Then run it either way. Both serve on **port 8080**, so run one at a time.

### Option 1 — Gradle

```bash
./gradlew bootRun
```

No JDK setup required: Gradle downloads the Java 25 Microsoft Build of OpenJDK
toolchain on first build. DevTools is active, so edits to `src/main` restart the
app automatically. Activates the `local` profile.

### Option 2 — Docker

Run `./gradlew build` first. It packages the jar the Dockerfile copies and
regenerates the root `.env` holding `APP_VERSION`, which is read by
`docker compose up --build -d` below.

```bash
./gradlew build
docker compose up --build -d      # build and start
docker compose logs -f app        # follow the logs
docker compose down               # stop and remove
```

This needs both toolchains, not Docker alone: the image does not compile
anything, so the Gradle build above — and the GitHub Packages credentials it
requires — is a prerequisite. The `builder` stage only explodes the packaged jar
into cacheable layers. Activates the `docker` profile.

### Verify either one

```bash
curl http://localhost:8080/api/v1/helloworld
# {"message":"Hello World!"}

curl http://localhost:8080/actuator/health
# {"status":"UP"}
```

### Stopping it

**Gradle** — <kbd>Ctrl</kbd>+<kbd>C</kbd> in the `bootRun` terminal. Shutdown is
graceful, but its logs are discarded and Gradle then prints `BUILD FAILED`. Both
are expected; [BUILD.md](BUILD.md#stopping-the-application) explains why, and
how to watch the shutdown instead.

**Docker** — `docker compose down`. SIGTERM reaches the JVM as PID 1, so the
same graceful shutdown applies.

Credentials are only ever needed at **build** time; see
[BUILD.md](BUILD.md#github-packages-credentials).

## The stack

| Layer      | Choice                                            |
|------------|---------------------------------------------------|
| Language   | Java 25 (Microsoft Build of OpenJDK toolchain)    |
| Framework  | Spring Boot 4.1.1 — Web MVC, Actuator, Validation |
| Build      | Gradle 9.7.1, Kotlin DSL                          |
| Versions   | Shared catalog `com.rubensgomes:gradle-catalog`   |
| Testing    | JUnit 5, Mockito, AssertJ, Spring Test            |
| Coverage   | JaCoCo, enforced at 90% line and branch           |
| Formatting | Spotless — Google Java Format, ktfmt, ktlint      |
| Analysis   | SonarCloud                                        |
| Release    | `net.researchgate.release`                        |
| Publishing | GitHub Packages                                   |
| Container  | Two-stage Docker build on eclipse-temurin JRE     |
| CI         | GitHub Actions — verify on push, manual release   |

## Versioning

The build uses locked dependency versions. Any catalog change that moves a
library or framework version requires regenerating the lock files:

```bash
./gradlew :app:dependencies --write-locks
```

Locking runs in strict mode — a missing lock file fails the build rather than
quietly resolving whatever is newest. Details in
[BUILD.md](BUILD.md#dependency-locking--reproducible-resolution).

## Common commands

| Command                                     | Purpose                                              |
|---------------------------------------------|------------------------------------------------------|
| `./gradlew bootRun`                         | Run locally on port 8080                             |
| `./gradlew test`                            | Run the suite; coverage report always follows        |
| `./gradlew build`                           | Format check + tests + coverage gate + all artifacts |
| `./gradlew spotlessApply`                   | Reformat sources                                     |
| `./gradlew publishToMavenLocal`             | Install to `~/.m2`                                   |
| `./gradlew release`                         | Tag and bump the version (prefer the workflow)       |
| `./gradlew :app:dependencies --write-locks` | Regenerate the `:app` dependency lock files          |
| `gh workflow run build-verify.yml`          | Run the build + Sonar gate in CI                     |
| `gh workflow run release.yml`               | Cut a release from CI                                |
| `gh workflow run acr-build-deploy.yml`      | Build and push the image to ACR                      |
| `docker compose up --build -d`              | Build and run the image — `./gradlew build` first    |
| `docker compose down`                       | Stop and remove the container                        |

## Project layout

```
azure-acr/
├── Dockerfile                 # two-stage image: builder, runtime
├── docker-compose.yml         # local container up/down
├── .dockerignore              # build-context exclusions
├── .env                       # generated by ./gradlew build (gitignored)
├── settings.gradle.kts        # inclusion, repositories, version catalog
├── settings-gradle.lockfile   # lock state: version catalog resolution
├── gradle.properties          # developer identity, license, SCM, Sonar, daemon
├── BUILD.md                   # build documentation
├── llms.txt                   # machine-readable project index
├── misc/tasks/                # plans and outstanding work
├── .github/
│   └── workflows/             # stubs; bodies live in rubensgomes-org/azure-workflows
│       ├── acr-build-deploy.yml  # build the jar, then az acr build
│       ├── acr-repo-delete.yml   # DESTRUCTIVE: delete an ACR repository
│       ├── build-verify.yml      # build + sonar
│       └── release.yml           # ./gradlew release
└── app/
    ├── build.gradle.kts       # the entire build
    ├── gradle.lockfile        # lock state: application dependencies
    ├── buildscript-gradle.lockfile  # lock state: plugin classpath
    ├── gradle.properties      # coordinates, version
    └── src/
        ├── main/resources/
        │   ├── application.yml             # defaults (quiet: root=error)
        │   ├── application-local.yml       # bootRun profile
        │   └── application-docker.yml      # container profile
        ├── main/java/com/rubensgomes/azure/acr/
        │   ├── App.java                    # @SpringBootApplication entry point
        │   ├── event/                      # lifecycle listeners
        │   ├── model/response/             # response types
        │   ├── service/                    # business layer
        │   └── web/controller/             # REST layer + /error handler
        └── test/java/...                   # mirrors main
```

The single subproject is named `app` so the layout stays valid whatever the
project is called; the published artifact takes its name from the `artifactId`
property instead.

## Documentation

| Document                       | Contents                                                        |
|--------------------------------|-----------------------------------------------------------------|
| [BUILD.md](BUILD.md)           | Every Gradle task, when it runs, how to run it, troubleshooting |
| [llms.txt](llms.txt)           | Machine-readable index for AI coding assistants                 |
| [LICENSE](LICENSE)             | MIT terms, plus AI-content and copyright-status notices         |
| [DISCLAIMER.md](DISCLAIMER.md) | General AI-generated content disclaimer                         |

## License

[MIT License](LICENSE). Author: [Rubens Gomes](https://rubensgomes.com).

This project was developed primarily with AI-assisted code generation; all
generated content was reviewed, tested, and refined by human contributors. The
[LICENSE](LICENSE) file carries the full AI-content, third-party content, and
copyright-status notices — read it rather than the SPDX tag alone.

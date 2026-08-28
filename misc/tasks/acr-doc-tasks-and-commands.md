# Expand ACR.md: ACR Tasks rationale and az command reference

`ACR.md` explains what the registry is and how this project names its images,
but it never states why the build itself runs *inside* ACR. It also gives the
reader no way to check the registry from a terminal.

Two additions, documentation only. No workflow, Dockerfile or build change.

## Tasks

- [x] Add a **Why ACR Tasks** subsection after the existing capability list,
      giving reasons beyond "cloud-based builds":
      no Docker daemon or `docker login` on the runner; only the build context
      is uploaded, and the push is part of the build; credentials stay as an
      Azure RBAC role instead of a registry password on the runner; builds run
      on Azure-managed compute in the registry's region, so base-image pulls and
      the push never cross the internet; `acr purge` and other maintenance run
      as tasks with no runner at all; base-image update triggers can rebuild an
      image when its base is patched; the same task runs identically from a
      laptop and from CI, which is what makes builds reproducible.
      Also record the cost: ACR Tasks runs the CLASSIC Docker builder, so
      BuildKit-only syntax (`--mount`, cache mounts) is rejected — the reason
      `Dockerfile` carries the `RUN true` separators and `BUILD.md` warns about
      the dropped Gradle cache mount.

- [x] Add an **az Commands** section with three groups, all using this
      project's real coordinates (`rubensdevacr`, `dev/azure-acr`):
      - registry: `az acr list`, `az acr show`, `az acr show-usage`
      - repository: `az acr repository list`, `show-tags`, `show --image`
        (digest read-back), `show-manifests`
      - smoke test: `az acr run --cmd "--entrypoint java $Registry/<image>
        --version" /dev/null`, noting that `$Registry` is substituted by ACR
        Tasks and not by the shell, that `/dev/null` is the source location for
        a task with no build context, and that the ENTRYPOINT override is
        needed because the image otherwise boots the application.

## Review

Both sections are in `ACR.md`. Documentation only — no workflow, `Dockerfile`
or build change.

**Why ACR Tasks** sits between the capability list and *ACR Settings*, so the
reader learns why the build runs inside the registry before reading how images
are named. The caveat paragraph is kept with the reasons rather than buried:
the classic builder is the direct cause of two things a reader will otherwise
find surprising in `Dockerfile` and `BUILD.md`.

**az Commands** is the last section, grouped registry / repository / smoke
test. Every command is checked against the installed `az`; the coordinates are
this project's real ones. One correction made while writing:
`az acr manifest list-metadata` takes the REPOSITORY in `--name` and the
registry in `--registry`, not a combined `registry/repo` value — noted inline,
along with the fact that the command group is still in preview.

The smoke test mirrors the one `rubensgomes-org/azure-workflows`
`acr-build-deploy.yml` runs, so a manual check and CI exercise the same path.
Its three footguns — `$Registry` being substituted by ACR Tasks and not bash,
`/dev/null` as the source location, and the mandatory `--entrypoint java` —
are spelled out because each fails confusingly rather than obviously.

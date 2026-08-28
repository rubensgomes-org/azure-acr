## Azure Container Registry (ACR)

We require a consistent way of building and maintaining container images. We do
not want images built locally and pushed from developer workstations, leading to
inconsistent builds and unclear versioning.

Clients expect reliable deployments with traceable image versions and the
ability to roll back to previous releases. We therefore need a centralized
registry that supports cloud-based builds, enforces consistent tagging, and
integrates with our deployment pipelines.

ACR is a managed, private Docker registry service that stores and distributes
container images and related artifacts. It provides direct control over access,
geo-replication for global distribution, and integration with Azure services
like Azure Kubernetes Service and Azure Container Apps.

ACR offers several important capabilities:

- Private storage: Keep model serving images, preprocessing containers, and
  inference APIs secure within your Azure environment
- Geo-replication: Distribute images close to deployment regions for faster
  pulls and reduced latency. Note that geo-replication requires the Premium
  service tiers.
- Integration: Connect directly to Azure Kubernetes Service, Azure Container
  Apps, and Azure App Service for seamless deployments
- Content formats: Store Docker images, Helm charts, and OCI artifacts in a
  single registry
- Multi-layers: ACR shares common layers across images, reducing storage costs
  and speeding up pulls.

### Why ACR Tasks

`az acr build` does not build locally and push. It uploads the build context to
ACR Tasks and the registry builds the image on Azure-managed compute, which is
how this project keeps builds off developer workstations. Beyond that:

- No Docker on the runner: no daemon to install, no `docker login`, and no
  separate `docker push`. Build and push are one server-side operation.
- Only the build context is uploaded, so `.dockerignore` decides what leaves the
  runner. Nothing else crosses the network.
- Credentials are an Azure RBAC role assignment on a service principal, not a
  long-lived registry password sitting in the runner's Docker config.
- The build runs in the registry's own region, so base-image pulls and the final
  push stay inside Azure rather than traversing the public internet.
- Maintenance runs the same way with no runner at all: `acr purge` deletes the
  untagged manifests that re-pushing a tag orphans, which otherwise keep billing
  against the registry quota.
- Base image update triggers can rebuild an image automatically when the base it
  was built `FROM` is patched, without a commit.
- The same command behaves identically from a laptop and from CI, which is what
  makes the published image reproducible and its version traceable.

One cost is worth knowing up front. ACR Tasks runs the **classic** Docker
builder, not BuildKit, so BuildKit-only syntax is rejected — `RUN --mount`
included. This is why `Dockerfile` separates its consecutive `COPY --from`
instructions with `RUN true`, and why the Gradle cache mount described in
`BUILD.md` is gone.

### ACR Settings

1. Create the ACR registry in the same Azure region in which you deploy your
   container.
2. Place the ACR registry in its own resource group.  Refer to the azure-iac 
   project for more information about the resource group of an ACR registry.
3. Build and deployment of artifacts should use headless service identity (e.
   g., using a Service Principal)

#### Registry:

- registry name: rubensdevacr (defined in the azure-iac project)
- login server URL: rubensdevacr.azurecr.io

#### Repository:

- repository name: azure-acr (same as project name)
- dev/qa:
    - namespace: dev (used for development purposes)
    - example: <dev|qa>/azure-acr

- stage/prod:
    - namespace: stage or prod
    - example: <stage|prod>/azure-acr

#### Tags:

- dev/qa:
    - semantic: 0.0.1-SNAPSHOT (same as app version)

- stage/prod:
    <!-- @formatter:off -->
    - digest sha:0a2e01852872580b2c2fea9380ff8d7b637d3928783c55beb3f21a6e58d5d108
    <!-- @formatter:on -->

### Sample Artifacts

- rubensdevacr.azurecr.io/dev/azure-acr:0.0.1-SNAPSHOT

<!-- @formatter:off -->
- rubensdevacr.azurecr.io/prod/azure-acr@sha256:0a2e01852872580b2c2fea9380ff8d7b637d3928783c55beb3f21a6e58d5d108
<!-- @formatter:on -->

### az Commands

All commands below assume `az login` has been done and the subscription holding
the registry is selected.

#### Check and list the registry

```bash
# Every registry the signed-in identity can see, with its login server.
az acr list --output table

# One registry. Fails if it does not exist, so it doubles as an existence check.
az acr show --name rubensdevacr --output table

# Read the login server back from Azure rather than assembling
# "<name>.azurecr.io", which hardcodes the public-cloud suffix.
az acr show --name rubensdevacr --query loginServer --output tsv

# Storage consumed against the service tier quota.
az acr show-usage --name rubensdevacr --output table
```

#### Check and list the repository

```bash
# Every repository in the registry, namespace included (e.g. dev/azure-acr).
az acr repository list --name rubensdevacr --output tsv

# Tags of one repository.
az acr repository show-tags --name rubensdevacr \
  --repository dev/azure-acr --output tsv

# The digest a tag currently points at. Use this to confirm a push landed:
# "az acr build" has reported success while the push did not.
az acr repository show --name rubensdevacr \
  --image dev/azure-acr:0.0.1-SNAPSHOT --query digest --output tsv

# Manifests, including the untagged ones left behind by re-pushing a tag.
# Here --name is the REPOSITORY; the registry is --registry. Preview command.
az acr manifest list-metadata --registry rubensdevacr \
  --name dev/azure-acr --output table
```

#### Smoke test an image with an ACR task

`az acr run` runs a one-off task in the registry, so the image is pulled and
started by ACR itself. That proves the layers are pullable and the JVM starts,
with no Docker on the machine issuing the command.

```bash
az acr run \
  --registry rubensdevacr \
  --cmd "--entrypoint java \$Registry/dev/azure-acr:0.0.1-SNAPSHOT --version" \
  /dev/null
```

Three details are easy to get wrong:

- `$Registry` is substituted by ACR Tasks, not by the shell. Quote it so bash
  leaves it alone.
- `/dev/null` is the source location. The task needs no build context, and
  passing `.` would upload the whole project for nothing.
- `--entrypoint java` is required. The image ENTRYPOINT is
  `java -XX:AOTCache=app.aot -jar application.jar`, so without the override the
  smoke test boots the entire application instead of answering `--version`.

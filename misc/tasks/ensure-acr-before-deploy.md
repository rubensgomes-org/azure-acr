# Ensure the ACR Exists Before Deploying the Image — SUPERSEDED

> **Status: superseded on 2026-08-27.** The cross-repo infrastructure gate
> described below was built, then removed. `acr-build-deploy.yml` now *assumes*
> the registry exists and verifies it, rather than provisioning it.
> This file is kept as a record of the decision and its reversal.

## What was originally built

`acr-build-deploy.yml` had two jobs. An `infra` job called
`rubensgomes-org/azure-iac`'s `acr-create.yml` as a cross-repo reusable workflow
(pinned to `@v0.4.1`), bringing azure-iac modules 01 (resource groups), 04
(managed identities) and 06 (the registry) to their desired state under
`terraform apply -auto-approve`, and returning the registry coordinates as job
outputs. A `build` job then consumed those outputs via `needs: infra`.

The four Azure secrets were passed explicitly rather than with
`secrets: inherit`, so the call site documented exactly which credentials
crossed the repository boundary.

## Why it was removed

Provisioning and deployment were doing two different jobs in one workflow, and
the deploy side did not need the coupling: the registry is long-lived, while a
deploy runs many times against it. Running `terraform apply` against the
subscription on every image build was a large blast radius for the benefit.

Creating and maintaining the registry stays authored and versioned in the
`azure-iac` repository. This repository only pushes to it.

## What the workflow does now

- `registry_name` input, defaulting to `rubensdevacr`.
- A **Verify the registry exists** step runs `az acr show` after sign-in. It is
  the existence check and the login-server lookup at once, and it fails with the
  cause named — including a list of the registries the service principal *can*
  see — rather than letting a missing registry surface minutes later as an
  opaque `az acr build` error.
- The login server is read back from Azure, never assembled as
  `<name>.azurecr.io`, which would hardcode the public-cloud suffix.
- After the push, a second step reads the manifest back with
  `az acr repository show` to confirm the tag actually landed.

Nothing in this repository provisions Azure resources any more.

## Facts still worth keeping

- The registry is `rubensdevacr`; its login server is `rubensdevacr.azurecr.io`.
- `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID` and
  `AZURE_SUBSCRIPTION_ID` are **organization** Actions secrets on
  `rubensgomes-org`, shared with this repository — not repo secrets.
- The service principal has subscription-wide write access, which is why the
  sign-in step names any missing secret explicitly and prints only its NAME.
- `az login --service-principal` is used rather than `azure/login@v2`: the four
  secrets are separate values, and v2 wants them pre-assembled into one `creds`
  JSON blob.

## Related

- [extract-setup-java-gradle-build-action.md](extract-setup-java-gradle-build-action.md)
  — the composite actions `acr-build-deploy.yml` now uses to build the jar.

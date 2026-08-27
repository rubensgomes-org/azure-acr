# acr-repo-delete.yml — delete one ACR repository

Deletes `<environment>/<artifactId>` — every tag and manifest in it — from an
existing registry. Manual only, guarded twice, unrecoverable. The registry
itself is never touched; that is azure-iac's `acr-destroy.yml`.

## Tasks

- [x] Create `.github/workflows/acr-repo-delete.yml` (`name: acr-repo-delete`)
- [x] Inputs: `environment` (default dev), `registry_name` (default
      rubensdevacr), `confirm` (required, no default). No `tag`.
- [x] Step: check out the repository (needed only to read `app/gradle.properties`)
- [x] Step: resolve the repository coordinates from `environment` + `artifactId`
- [x] Step: SAFEGUARD — actor allowlist plus the typed phrase
      `DELETE REPO <environment>/<artifactId>`, both before any credential
- [x] Step: sign in to Azure (block reused from `acr-build-deploy.yml`)
- [x] Step: verify the registry exists (block reused from `acr-build-deploy.yml`)
- [x] Step: check whether the repository exists, and log its tags if it does
- [x] Step: delete, guarded on that check
- [x] Step: report the outcome and read the catalog back
- [x] Validate the YAML parses and the shell logic behaves
- [ ] Dispatch-test: wrong phrase, no-op path, real delete (see Review)

## Review

Built as planned. Two decisions worth recording:

**Existence is checked with `az acr repository list`, not `... repository show`.**
`show` exits non-zero for a missing repository AND for a network or token
failure. Since an absent repository is a green no-op here, `show` would turn an
Azure outage into a silent success that deleted nothing and said so cheerfully.
`list` exits zero on success, so absence is a real answer and a failure still
fails the run. The name is matched with `grep -Fxq` so `dev/azure-acr` cannot
match `dev/azure-acr-demo`.

**The confirmation phrase is built from the resolved repository**, not from a
second typed input. Comparing the typed phrase against anything other than what
the delete will actually act on would let the guard pass while a different
repository is removed.

Inputs reach shell steps only through `env:` bindings. A `${{ }}` expression
inside a `run:` body is substituted as raw text before bash parses it, so a
crafted value would execute on the runner — in a workflow that deletes things,
with credentials in scope.

Not yet dispatch-tested. In order: (1) wrong `confirm` — expect denial at the
safeguard with no sign-in step having run; (2) `environment: doesnotexist` with
the matching phrase — expect green, delete skipped; (3) a real delete against a
throwaway `scratch/azure-acr` published by `acr-build-deploy`, then confirm
`dev/azure-acr` survives.

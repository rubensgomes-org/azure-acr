# Share CI/CD workflows via rubensgomes-org/azure-workflows

Ten repos under `rubensgomes-org` duplicate the same CI/CD by copy-paste. Extract the
four workflows and their composite actions into one public, versioned repo consumed
through thin `workflow_dispatch` stubs.

Full plan: `~/.claude/plans/i-have-a-few-effervescent-marshmallow.md`

## Decisions

- Repo `rubensgomes-org/azure-workflows`, **public** (lets the personal-account repos
  consume it, and avoids Actions access-policy setup).
- All four workflows shared, plus four composite actions.
- Callers map secrets **explicitly**. `secrets: inherit` only forwards
  identically-named secrets, so it would not map `RUBENS_PAT_TOKEN` onto a declared
  `packages-token`. Matches the convention in `ensure-acr-before-deploy.md`.
- `permissions` and `concurrency` stay in the caller stub: a reusable workflow can only
  hold <= the caller's permissions, and `github.workflow` resolves to the caller.
- Composite actions inside a reusable workflow MUST be referenced by full path
  (`rubensgomes-org/azure-workflows/.github/actions/X@ref`), never `./` -- inside a
  reusable workflow `actions/checkout` checks out the *caller's* repo.

## Phase A - create and populate azure-workflows

- [x] A1. Create the public repo; seed README, LICENSE, CHANGELOG, actionlint lint.yml
- [x] A2. Port `setup-java-gradle` unchanged; port `build` as `gradle-build` + `project-path` input
- [x] A3. Extract `azure-login` and `verify-acr-registry` composite actions
- [x] A4. Author the four reusable workflows (`on: workflow_call`)
- [x] A5. Write README as the consumer contract (inputs, secrets, copy-paste stubs)
- [x] A6. Push; actionlint green

## Phase B - pilot azure-acr

- [x] B1. Stubs pinned `@main` (a reusable workflow cannot be tested before it is pushed)
- [x] B2. Replace the four workflow files with stubs
- [x] B3. Delete `azure-acr/.github/actions/`
- [x] B4. Update stale docs: README.md, BUILD.md, llms.txt, TODO.md
- [x] B5. Dispatch-test all four (guards first on acr-repo-delete)

## Phase C - tag v1

- [x] C1. Tag `v1.0.0` + moving `v1`; add the tag-major automation
- [x] C2. Repin azure-acr stubs `@main` -> `@v1`

## Phase D/E - roll out

- [x] D1. spring-blueprint (template repo, so future clones are born correct)
- [x] E1. Remaining 8 azure-* repos

## Review

Done. `rubensgomes-org/azure-workflows` is public and tagged `v1.0.0`, with `v1`
moving onto it. All ten repositories consume it and are pinned `@v1`; no
repository keeps a local `.github/actions/`.

`azure-acr` went from 1332 lines of workflow and action YAML to 197. The other
nine went from ~330 each to 78.

### Decisions taken during implementation, beyond the plan

- **Internal action references pin `@v1`, not `@main`.** The plan did not settle
  this. Left at `@main`, a consumer pinning `@v1` would have received v1 workflow
  bodies driving `main` actions -- the pin would have bought nothing. Because
  `v1` moves onto each `v1.x.y` (`tag-major.yml`), `@v1` is self-consistent and
  needs no per-release chore; only a major bump touches those references.
  `lint.yml` fails the build if they ever disagree.
- **shellcheck stays enabled in `lint.yml`.** The first draft disabled it wholesale
  because `$GRADLE_ARGS` must word-split. It reported only five findings, so the
  four deliberate ones carry inline `shellcheck disable=` with a reason instead.
  The fifth was real: `${PROJECT_PATH}` was unquoted and is now quoted.
- **`tag-major.yml` added.** Not in the plan, but a moving `v1` has to be
  maintained by something, and by hand it would eventually be forgotten.
- **The nine converted repos adopted dispatch-only**, confirmed with the user
  rather than assumed. They also now run `:app:build`, which they did not before:
  the shared `gradle-build` action produces `applicationJar` and `generateDotEnv`
  for the ACR workflow. Both are gitignored, so nothing is dirtied.
- **`spring-blueprint` got only the two Gradle stubs**, so future clones do not
  inherit a destructive `acr-repo-delete` button they may never need.

### Verified live

- `build-verify` on `azure-acr` and on `azure-aca` (identical stub, proving they
  are repo-agnostic): five red/green phases, `project-path` resolving to `:app`.
- `acr-build-deploy`: pushed `dev/azure-acr:0.0.6-SNAPSHOT`, digest verified, the
  smoke test printed the JVM version -- so the `--entrypoint java` AOT override
  survived the move -- and the purge reported `Number of deleted tags: 0`, i.e.
  it did not remove the tag just pushed.
- `acr-repo-delete`, all three paths: a wrong phrase failed at the SAFEGUARD step
  with "Sign in to Azure" SKIPPED (Azure never contacted); an absent repository
  exited green having skipped the delete; and a real deletion, against a
  throwaway `scratch/azure-acr` built for the purpose rather than against `dev`,
  was confirmed by the catalog read-back.

### Not verified

- **`gradle-release` has never run through the shared workflow.** Nothing was
  dispatched, because a release cuts a real version and pushes two commits and a
  tag to `main`, and `requireBranch = "main"` leaves no safe rehearsal. The next
  real release is the test. If it fails, the fallback is to pin that one stub
  back to a commit of the previous local `release.yml`.
  Watch three things: the git identity read out of `gradle.properties`; that the
  push succeeds, i.e. the PAT was persisted by checkout; and that
  `RUBENS_PAT_TOKEN` carries `repo` scope, not only `read:packages`.

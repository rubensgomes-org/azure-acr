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
- [ ] B5. Dispatch-test all four (guards first on acr-repo-delete)

## Phase C - tag v1

- [ ] C1. Tag `v1.0.0` + moving `v1`; add the tag-major automation
- [ ] C2. Repin azure-acr stubs `@main` -> `@v1`

## Phase D/E - roll out

- [ ] D1. spring-blueprint (template repo, so future clones are born correct)
- [ ] E1. Remaining 8 azure-* repos

## Review

_(to be filled in when wrapping up)_

# Fix stale comments left by the build-verify trigger change

Commit `082dc7e` ("Make build-verify manually triggered") dropped the `push`
trigger from `build-verify.yml`. Three comments in `release.yml` still describe
the old behaviour, and two of them are the stated justification for using a PAT
as the checkout token. Separately, `.dockerignore` still excludes a file that no
longer exists.

Scope is comments and one ignore entry only -- no behavioural change. The PAT
stays; see the decision note below.

## Tasks

- [x] `release.yml:33-34` -- drop the claim that each release triggers ~2 runs
      of `build-verify.yml`.
- [x] `release.yml:58-61` -- rewrite the `permissions:` NOTE. The push no longer
      needs to trigger anything.
- [x] `release.yml:113-116` -- rewrite the checkout `token:` comment for the
      same reason.
- [x] `.dockerignore:70-72` -- remove the `acr-smoke-test.yaml` entry and its
      comment. The file does not exist; the smoke test uses
      `az acr run --cmd` with `/dev/null` as its source location.

## Decision -- keep the PAT

The automatic per-run token could now do this push: `permissions: contents:
write` is already granted, and there is no longer a downstream workflow that a
`GITHUB_TOKEN` push would fail to trigger.

It stays anyway. Swapping the credential is invisible when it works and awkward
to debug when it does not, and the PAT keeps the door open if a push trigger is
ever restored. The comments are reworded to present it as deliberate headroom
rather than a hard requirement.

## Review

All four items done. Comments and one ignore entry only -- verified no
behavioural change: the non-comment lines of `release.yml` are byte-identical to
the previous commit, and `acr-smoke-test.yaml` does not exist, so dropping its
`.dockerignore` entry cannot alter the build context `az acr build` uploads.

The PAT stays, as decided above. Both comments that referenced it now say the
per-run token would suffice today and explain why the PAT is kept regardless,
so the next reader does not mistake headroom for a hard requirement.

Not touched, still open from the wider discussion: making `acr-build-deploy.yml`
callable from another repository via `workflow_call`.

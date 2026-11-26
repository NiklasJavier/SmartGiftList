# Contributing to SmartGiftList

SmartGiftList is an archived reference repository showcasing an early Java/JSP web application modernized for reproducible local builds and end-to-end verification.

## Local Verification Workflow

```bash
nix develop
just doctor
just check
```

1. Follow [Conventional Commits](https://www.conventionalcommits.org/) for commit messages.
2. Never hardcode local home directories, database credentials, or personal metadata — run `just check-privacy` before submitting changes.
3. Verify all Markdown links, shell scripts, and Gradle unit tests pass via `just check`.

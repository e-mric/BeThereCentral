# Contributing

Start with the [README](README.md), [PRD](docs/PRD.md) and [backlog](docs/BACKLOG.md). A small documentation improvement or one feature with a behavioral test is a good first contribution. The sample app needs no account, secret, real building plan or positioning hardware.

## Workflow

1. Describe the user problem and expected behavior in an issue or local backlog entry. For experiments, name the assumption and evidence that would change our decision.
2. Make a small change on a branch. Keep domain behavior free of Compose and transport details; place code under its feature.
3. Add/update behavior tests in the matching feature package under `commonTest`, or `server/tests/sharing` for the reference server. Check invalid input, empty states and privacy boundaries. Avoid implementation snapshots and mock-heavy tests.
4. Run the relevant checks in `docs/testing/STATUS.md`. Record what actually passed; distinguish build, simulator launch and real-device verification.
5. Update affected documentation and `CHANGELOG.md` in the same pull request. Keep the README capability table honest.
6. Request review against both the PRD and these standards. Include screenshots for UI changes and clear testing evidence.

Keep dependencies minimal and pinned. Do not add analytics, persistent location storage, network uploads or production authentication under the label of a demo. Sample data must remain visibly identified. Document a new third-party source/license before committing assets.

## Design conventions

Use stable IDs across maps, graph routes, QR checkpoints and future 3D. Favor small public interfaces that hide meaningful behavior. Don't create repository/use-case wrappers that only forward calls. Dependencies point from platform/data/presentation into the domain. Test directories mirror feature directories because Kotlin Multiplatform needs distinct source sets.

## Public collaboration

Be respectful, specific and constructive. Report security issues without posting live credentials or real people's positions. Before enabling GitHub publishing, configure an actual remote and maintainer contact. Contributions are under the repository's MIT license; vendored skills keep their upstream notices.

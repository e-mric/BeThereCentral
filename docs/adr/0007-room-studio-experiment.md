# ADR 0007: Bounded local Room Studio experiment

Status: accepted · 8 October 2026

## Context

The creator wants to explore tenant-style room decoration and asked whether a courtyard garden might be shown at a smaller apparent scale from higher floors. The supplied plan labels and approximate anchors do not establish exact room-to-company assignments, and the active five-floor artwork is fictional. A production tenant editor would require confirmed room mapping, permissions, accounts, asset rights, review and publishing controls.

## Decision

Build a bounded one-room editor prototype using one shared versioned JSON fixture in the browser and Compose. The fictional demo room has an immutable convex chamfered outline. Its company sign and a bounded set of furniture objects (desk, plant, sofa, rug) are editable. The browser Studio supports drag and keyboard placement, add/delete, undo/redo, local save/reload, and JSON import/export. Compose opens a transient preview through More and accepts pasted/imported JSON or reset; it does not persist the preview.

Keep this experiment separate from the supplied floor datasets and directory. The starter room is not mapped to a company or actual campus room. Do not present the tool as available for tenant editing of their actual rooms. There is no AI generation, account system, shared save, backend, publishing, review workflow or public content delivery.

Keep courtyards empty and dark for now. On ground, preserve the full silhouette while leaving the grey source area unfurnished. A garden remains a future decorative option. If explored in a multi-floor perspective, size it according to apparent perspective so it reads smaller from higher floors. Do not imply a garden is present at BeCentral or that this scaling behavior is implemented.

## Consequences

The prototype can test the basic authoring and interchange flow without changing authoritative plan geometry or introducing user accounts. Browser saves remain local to that browser/device; moving a scene requires explicit JSON export/import. Compose is preview-only and resets when closed. Any future tenant deployment needs verified room mapping, authorization, asset rights, review, versioning, rollback and an owner for publishing. This ADR amends ADR 0006 only to permit a separate local editor experiment; it does not relax the plan-world constraints.

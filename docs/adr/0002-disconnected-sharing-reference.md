# ADR 0002: Disconnected sharing reference

Status: accepted · 7 October 2026

## Context

The product requires server-enforced expiry, selected recipients, revocation and no location history, but no identity provider, server deployment or key infrastructure exists. UI countdowns alone cannot enforce these requirements.

## Decision

Keep the app's sharing screen explicitly local. Add an optional Python standard-library reference server and behavior/HTTP tests for authorization, expiry and latest-payload storage. Bind it to loopback, create ephemeral demo identities, accept only opaque synthetic payloads and persist nothing. Do not connect it to real location data. Document the separate production E2EE design and its missing pieces.

## Consequences

The security-critical lifecycle can be exercised now without pretending a full integration exists. Python is a small deliberate exception to the Kotlin application stack; a production server framework/language is undecided. Opaque payload storage is not E2EE. Production sharing remains blocked on real identity, TLS, audited client crypto, device keys, offline semantics and independent review.

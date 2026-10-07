# Sharing privacy and threat model

## What exists

The app demonstrates opt-in recipient and duration selection locally. No recipient receives data. Its clock/countdown is UX simulation, not server enforcement. Only the latest checkpoint observation is used; the app has no continuous location integration or analytics.

The disconnected Python reference server implements server-clock grants, allowed durations of 5/10/15 minutes, recipient checks, owner-only updates/revocation and one latest opaque payload in memory. Every read/write checks expiry, including the exact expiry instant. Both wall and monotonic deadlines are checked, so a backward wall-clock correction cannot extend consent. A one-second sweep deletes idle expired grants. Revocation deletes the grant and payload under the same lock; subsequent reads and writes are denied. A read already completed before revocation cannot be recalled. Restart discards all data. There is no database, history endpoint, request logging or backup.

Physical memory erasure is not guaranteed by Python. Expired grants become logically inaccessible immediately and references are removed on access or sweep; this is not secure memory zeroization. OS dumps and compromised hosts remain outside the reference guarantee.

## Intended production flow — unimplemented

1. Authenticate sender and recipients using a reviewed identity provider. Fetch authenticated, verifiable public keys; prevent a relay from silently substituting a key. Support key change warnings and device removal.
2. On explicit consent, create a short-lived server grant for selected identities. The server sets expiry from its trusted clock; clients cannot extend it by altering their clocks or payload timestamps.
3. Generate a distinct session key with the platform cryptographic RNG. Encrypt observations with an audited authenticated-encryption library (never custom cryptography). Bind building/version, grant, sender, recipient set and sequence to authenticated context. Use unique nonces and reject replay/out-of-order sequence values.
4. Deliver the session key only to selected, verified recipient devices through reviewed authenticated key agreement/key wrapping. Store private keys in platform secure storage.
5. Send only ciphertext envelopes to the relay. Replace the prior envelope; exclude ciphertext, room IDs, capabilities and keys from logging, analytics, crash reports and backups.
6. On revocation/expiry, deny all reads/writes and drop payloads/keys. If offline, local “stop” ends transmission immediately but remote revocation requires confirmation; show pending revocation honestly. Reconnection must not revive an expired grant.

**E2EE is designed for, not implemented.** Accepting a string named `ciphertext` is not encryption. Do not integrate real sharing until identity, TLS, client crypto, multi-device behavior and independent security review are complete.

## Threats and limits

| Threat | Prototype handling | Production work |
| --- | --- | --- |
| Client clock tampering | Reference server owns clock; UI simulation doesn't secure data | Trusted server time and monitoring; fail closed |
| Unselected recipient guesses grant ID | Identity + grant membership checked; random IDs | Real authentication, authorization, rate limiting |
| Sender forgets to stop | Reference expires at 5/10/15 minutes | Integrated timer/status, reliable server deletion |
| Untrusted relay reads locations | Only opaque test payloads; no real E2EE | Verified keys and audited authenticated encryption |
| Recipient copies a position | Cannot be prevented/recalled | Clear consent language and trustworthy recipients |
| QR replaced or copied | Known payload validation only; not proof of physical presence | Signed/versioned payload policy, on-site integrity checks |
| App restart/background | In-memory demo state resets | Explicit lifecycle and permission design, no silent resumption |
| Stale accessibility/map data | Fictional and unverified | Survey, owner review and connector outage updates |

A future relay still sees metadata such as participant identities, timing and payload sizes; E2EE does not hide these. Define minimum retention and jurisdiction with the real deployment owner before a pilot. This document specifies engineering goals rather than asserting regulatory compliance.

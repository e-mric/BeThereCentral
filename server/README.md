# Reference sharing server

Runnable, loopback-only demonstration of grant authorization and **server-enforced** expiry. **Not connected to the Compose app. No production authentication, TLS or implemented E2EE.** Python 3.9+ standard library only; this keeps contributors from needing another backend dependency stack for the experiment.

From repository root:

```sh
python3 -m unittest discover -s server/tests -t . -v
python3 -m server
```

Startup prints fresh demo bearer tokens for `alice`, `bob`, and `charlie`. They are local ephemeral test credentials and change on restart. Stop with Ctrl+C. The app does not read these tokens.

All requests require `Authorization: Bearer <demo-token>`. Responses carry `Cache-Control: no-store`. Bind address is fixed to `127.0.0.1:8765`. Do not expose it publicly or use real personal data.

| Request | Body | Behavior |
| --- | --- | --- |
| `POST /grants` | `{"recipients":["bob"],"minutes":5}` | Authenticated sender grants selected known recipients access; returns random `id` and server `expiresAt` (Unix seconds) |
| `PUT /grants/{id}` | `{"ciphertext":"synthetic-envelope"}` | Owner replaces latest opaque test payload |
| `GET /grants/{id}` | None | Selected recipient reads latest payload (initially `null`) and expiry |
| `DELETE /grants/{id}` | None | Owner immediately revokes and deletes |

Only 5, 10 and 15 minutes are accepted. Request-supplied expiry is ignored. Unknown, revoked, expired and unauthorized grants return the same 404 response; missing/invalid bearer auth returns 401. Invalid input returns 400. The server stores one payload per grant and never logs requests. Inactive expired payload references are deleted within one second; access is denied at the exact deadline. Restart deletes all grants. The HTTP adapter caps bodies at 20 KB and the store at 1,000 concurrent grants; these are prototype limits, not production abuse protection.

See [privacy design](../docs/PRIVACY.md) before considering any integration. A future Kotlin/Ktor or other deployed adapter may implement the same contract after real identity and client encryption are ready. There is deliberately no location decryption or plaintext coordinate field in this server.

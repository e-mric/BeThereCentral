# Security and privacy status

This is a sample-building prototype, not a production location-sharing service. The mobile/desktop sharing screen is a local simulation. The separate reference server binds only to `127.0.0.1`, has ephemeral demo identities, and is not connected to the app.

Do not upload real positions or private keys to the reference server. It accepts opaque test strings but does not prove that they are encrypted, authenticate recipient keys, provide TLS, or protect against a compromised host. See [privacy design](docs/PRIVACY.md).

The repository is https://github.com/e-mric/BeThereCentral. A private security contact and GitHub private vulnerability reporting are not yet verified; the maintainer should configure them before a real-building pilot. Do not publish sensitive evidence in a public issue. Public reports about prototype bugs may use synthetic data.

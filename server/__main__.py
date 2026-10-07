from secrets import token_urlsafe
from .sharing.http import make_server


def main():
    tokens = {token_urlsafe(24): name for name in ("alice", "bob", "charlie")}
    server = make_server(tokens)
    print("BeThereCentral reference server — disconnected from the app; loopback only.")
    print("No production identity, TLS or implemented E2EE. Send test ciphertext only.")
    print("Ephemeral demo bearer tokens (discarded on restart):")
    for token, name in tokens.items():
        print(f"  {name}: {token}")
    print("Listening on http://127.0.0.1:8765")
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        pass
    finally:
        server.server_close()


if __name__ == "__main__":
    main()

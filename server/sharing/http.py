"""Loopback-only HTTP adapter. Ephemeral demo bearer identities, no production auth."""
import json
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from threading import Event, Thread
from urllib.parse import urlsplit

from .store import Denied, SharingStore


def make_server(tokens, store=None, port=8765):
    store = store or SharingStore()
    identities = frozenset(tokens.values())

    class Handler(BaseHTTPRequestHandler):
        def log_message(self, format, *args):
            pass  # Never log request URLs, capabilities, identities or ciphertext.

        def reply(self, status, body):
            encoded = json.dumps(body).encode()
            self.send_response(status)
            self.send_header("Content-Type", "application/json")
            self.send_header("Cache-Control", "no-store")
            self.send_header("Content-Length", str(len(encoded)))
            self.end_headers()
            self.wfile.write(encoded)

        def body(self):
            size = int(self.headers.get("Content-Length", "0"))
            if not 0 < size <= 20000:
                raise ValueError("Request size invalid")
            self.connection.settimeout(5)
            data = json.loads(self.rfile.read(size))
            if not isinstance(data, dict):
                raise ValueError("JSON object required")
            return data

        def handle_request(self):
            token = self.headers.get("Authorization", "")
            actor = tokens.get(token.removeprefix("Bearer ")) if token.startswith("Bearer ") else None
            if actor is None:
                return self.reply(401, {"error": "Authentication required"})
            path = urlsplit(self.path).path.strip("/").split("/")
            try:
                if self.command == "POST" and path == ["grants"]:
                    data = self.body()
                    recipients = data.get("recipients")
                    if not isinstance(recipients, list) or any(not isinstance(r, str) or r not in identities for r in recipients):
                        raise ValueError("Select known demo recipients")
                    return self.reply(201, store.create(actor, recipients, data.get("minutes")))
                if len(path) == 2 and path[0] == "grants":
                    key = path[1]
                    if self.command == "GET":
                        return self.reply(200, store.read(actor, key))
                    if self.command == "PUT":
                        store.publish(actor, key, self.body().get("ciphertext"))
                        return self.reply(200, {"updated": True})
                    if self.command == "DELETE":
                        store.revoke(actor, key)
                        return self.reply(200, {"revoked": True})
                self.reply(404, {"error": "Not found"})
            except Denied:
                self.reply(404, {"error": "Grant unavailable"})
            except (ValueError, TypeError, TimeoutError):
                self.reply(400, {"error": "Invalid request"})

        do_POST = do_GET = do_PUT = do_DELETE = handle_request

    server = ThreadingHTTPServer(("127.0.0.1", port), Handler)
    server.daemon_threads = True
    stopped = Event()

    def sweep():
        while not stopped.wait(1):
            store.purge()

    Thread(target=sweep, daemon=True).start()
    original_close = server.server_close

    def close():
        stopped.set()
        original_close()

    server.server_close = close
    return server

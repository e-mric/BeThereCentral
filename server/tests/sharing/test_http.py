import json
import threading
import unittest
from urllib.request import Request, urlopen
from urllib.error import HTTPError
from server.sharing.http import make_server
from server.sharing.store import SharingStore


class SharingHttpTests(unittest.TestCase):
    def setUp(self):
        self.now = 1000
        self.server = make_server({"alice-test": "alice", "bob-test": "bob", "charlie-test": "charlie"}, SharingStore(lambda: self.now), port=0)
        self.thread = threading.Thread(target=self.server.serve_forever, daemon=True)
        self.thread.start()

    def tearDown(self):
        self.server.shutdown()
        self.server.server_close()
        self.thread.join()

    def request(self, method, path, actor="alice", body=None):
        req = Request(f"http://127.0.0.1:{self.server.server_port}{path}",
                      data=json.dumps(body).encode() if body is not None else None,
                      headers={"Authorization": f"Bearer {actor}-test", "Content-Type": "application/json"}, method=method)
        try:
            response = urlopen(req, timeout=2)
        except HTTPError as error:
            response = error
        with response:
            self.assertEqual("no-store", response.headers["Cache-Control"])
            return response.status, json.load(response)

    def test_http_consent_recipient_isolation_expiry_and_revoke(self):
        status, grant = self.request("POST", "/grants", body={"recipients": ["bob"], "minutes": 5, "expiresAt": 9999999})
        self.assertEqual(201, status)
        self.assertEqual(1300, grant["expiresAt"])
        path = "/grants/" + grant["id"]
        self.assertEqual(200, self.request("PUT", path, body={"ciphertext": "test-envelope"})[0])
        self.assertEqual("test-envelope", self.request("GET", path, "bob")[1]["ciphertext"])
        self.assertEqual(404, self.request("GET", path, "charlie")[0])
        self.assertEqual(404, self.request("DELETE", path, "bob")[0])
        self.now = 1300
        self.assertEqual(404, self.request("GET", path, "bob")[0])
        _, second = self.request("POST", "/grants", body={"recipients": ["bob"], "minutes": 10})
        path = "/grants/" + second["id"]
        self.assertEqual(200, self.request("DELETE", path)[0])
        self.assertEqual(404, self.request("GET", path, "bob")[0])

    def test_http_authentication_and_validation(self):
        self.assertEqual(401, self.request("POST", "/grants", "nobody", {"recipients": ["bob"], "minutes": 5})[0])
        for body in [{"recipients": [], "minutes": 5}, {"recipients": ["bob"], "minutes": 60}, {"recipients": ["unknown"], "minutes": 5}, []]:
            self.assertEqual(400, self.request("POST", "/grants", body=body)[0])

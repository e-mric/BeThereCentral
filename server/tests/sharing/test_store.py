import unittest
from server.sharing.store import Denied, SharingStore


class SharingTests(unittest.TestCase):
    def setUp(self):
        self.now = 1000.0
        self.elapsed = 50.0
        self.store = SharingStore(lambda: self.now, lambda: self.elapsed)
        self.grant = self.store.create("alice", ["bob"], 5)
        self.key = self.grant["id"]

    def test_server_expiry_at_exact_boundary_blocks_reads_and_updates(self):
        self.store.publish("alice", self.key, "opaque-1")
        self.now = 1299.999
        self.assertEqual("opaque-1", self.store.read("bob", self.key)["ciphertext"])
        self.now = 1300
        with self.assertRaises(Denied):
            self.store.read("bob", self.key)
        with self.assertRaises(Denied):
            self.store.publish("alice", self.key, "opaque-2")

    def test_only_selected_recipient_can_read(self):
        for actor in ["alice", "charlie", ""]:
            with self.assertRaises(Denied):
                self.store.read(actor, self.key)

    def test_wall_clock_rollback_cannot_extend_sharing_duration(self):
        self.store.publish("alice", self.key, "opaque")
        self.now = 900.0
        self.elapsed = 349.999
        self.assertEqual("opaque", self.store.read("bob", self.key)["ciphertext"])
        self.elapsed = 350.0
        with self.assertRaises(Denied):
            self.store.read("bob", self.key)
        with self.assertRaises(Denied):
            self.store.publish("alice", self.key, "new")

    def test_only_owner_can_update_or_revoke(self):
        for actor in ["bob", "charlie"]:
            with self.assertRaises(Denied):
                self.store.publish(actor, self.key, "forged")
            with self.assertRaises(Denied):
                self.store.revoke(actor, self.key)

    def test_revoke_is_immediate_and_cannot_resurrect_grant(self):
        self.store.publish("alice", self.key, "opaque")
        self.store.revoke("alice", self.key)
        with self.assertRaises(Denied):
            self.store.read("bob", self.key)
        with self.assertRaises(Denied):
            self.store.publish("alice", self.key, "again")

    def test_overwrite_exposes_only_latest_payload(self):
        self.store.publish("alice", self.key, "first")
        self.store.publish("alice", self.key, "second")
        self.assertEqual({"ciphertext": "second", "expiresAt": 1300}, self.store.read("bob", self.key))

    def test_only_three_allowed_durations_and_explicit_recipients(self):
        for minutes, expected in [(5, 1300), (10, 1600), (15, 1900)]:
            self.assertEqual(expected, self.store.create("alice", ["bob"], minutes)["expiresAt"])
        for minutes in [0, -5, 6, 60, True, "5", 5.0]:
            with self.assertRaises(ValueError):
                self.store.create("alice", ["bob"], minutes)
        for recipients in [[], ["alice"], [""]]:
            with self.assertRaises(ValueError):
                self.store.create("alice", recipients, 5)

    def test_unknown_grant_denied(self):
        with self.assertRaises(Denied):
            self.store.read("bob", "unknown")

    def test_restart_has_no_saved_payloads(self):
        self.store.publish("alice", self.key, "opaque")
        fresh = SharingStore(lambda: self.now)
        with self.assertRaises(Denied):
            fresh.read("bob", self.key)

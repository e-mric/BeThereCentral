"""Small grant interface with a server-owned clock. No location decoding/history."""
from dataclasses import dataclass
from threading import RLock
from time import time, monotonic
from secrets import token_urlsafe
from typing import Callable, Dict, Iterable, Optional


class Denied(Exception):
    """Unknown, expired, revoked and unauthorized grants are indistinguishable."""


@dataclass
class Grant:
    owner: str
    recipients: frozenset
    expires_at: float
    elapsed_deadline: float
    payload: Optional[str] = None


class SharingStore:
    """Only latest opaque payload; callers must authenticate before crossing here."""

    def __init__(self, clock: Callable[[], float] = time, elapsed_clock: Callable[[], float] = monotonic):
        self._clock = clock
        self._elapsed_clock = elapsed_clock
        self._grants: Dict[str, Grant] = {}
        self._lock = RLock()

    def purge(self):
        with self._lock:
            now = self._clock()
            elapsed = self._elapsed_clock()
            for key in [k for k, g in self._grants.items() if now >= g.expires_at or elapsed >= g.elapsed_deadline]:
                del self._grants[key]

    def create(self, owner: str, recipients: Iterable[str], minutes: int):
        recipients = frozenset(recipients)
        if type(minutes) is not int or minutes not in (5, 10, 15):
            raise ValueError("Duration must be 5, 10 or 15 minutes")
        if not recipients or len(recipients) > 20 or owner in recipients:
            raise ValueError("Select 1–20 other recipients")
        if not owner or any(not isinstance(r, str) or not r or len(r) > 128 for r in recipients):
            raise ValueError("Invalid identity")
        with self._lock:
            self.purge()
            if len(self._grants) >= 1000:
                raise ValueError("Reference server capacity reached")
            key = token_urlsafe(24)
            grant = Grant(owner, recipients, self._clock() + minutes * 60, self._elapsed_clock() + minutes * 60)
            self._grants[key] = grant
            return {"id": key, "expiresAt": grant.expires_at, "recipients": sorted(recipients)}

    def _active(self, key: str) -> Grant:
        self.purge()
        grant = self._grants.get(key)
        if grant is None:
            raise Denied()
        return grant

    def publish(self, actor: str, key: str, ciphertext: str):
        if not isinstance(ciphertext, str) or not ciphertext or len(ciphertext) > 16000:
            raise ValueError("Ciphertext envelope must contain 1–16000 characters")
        with self._lock:
            grant = self._active(key)
            if actor != grant.owner:
                raise Denied()
            grant.payload = ciphertext

    def read(self, actor: str, key: str):
        with self._lock:
            grant = self._active(key)
            if actor not in grant.recipients:
                raise Denied()
            return {"ciphertext": grant.payload, "expiresAt": grant.expires_at}

    def revoke(self, actor: str, key: str):
        with self._lock:
            grant = self._active(key)
            if actor != grant.owner:
                raise Denied()
            del self._grants[key]

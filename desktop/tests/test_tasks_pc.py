import importlib.util
import io
import json
from pathlib import Path
import sys
import tempfile
import types
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import tasks_pc


class MergeTests(unittest.TestCase):
    def test_unrelated_remote_changes_survive_local_reopen(self):
        base = {"completed": {"a": True, "b": False}, "completedOn": {"a": "2026-11-01"}}
        local = {"completed": {"a": False, "b": False}, "completedOn": {}}
        remote = {"completed": {"a": True, "b": True}, "completedOn": {"a": "2026-11-01", "b": "2026-11-02"}}
        self.assertEqual(tasks_pc.merge_state(base, local, remote), {"completed": {"a": False, "b": True}, "completedOn": {"b": "2026-11-02"}})

    def test_inflight_new_edits_rebase_over_response(self):
        sent = {"completed": {"a": True}}
        latest = {"completed": {"a": False, "b": True}}
        response = {"completed": {"a": True, "phone": True}, "manualLinks": {"x": ["y"]}}
        merged = tasks_pc.merge_state(sent, latest, response)
        self.assertEqual(merged["completed"], {"a": False, "b": True, "phone": True})
        self.assertEqual(merged["manualLinks"], {"x": ["y"]})

    def test_tasks_merge_by_id_and_field_and_keep_remote_additions(self):
        base = {"customTasks": [{"id": "a", "title": "Original", "notes": "old"}]}
        local = {"customTasks": [{"id": "a", "title": "Original", "notes": "phone"}, {"id": "b", "title": "Local"}]}
        remote = {"customTasks": [{"id": "a", "title": "PC rename", "notes": "old"}, {"id": "c", "title": "Remote"}]}
        tasks = {x["id"]: x for x in tasks_pc.merge_state(base, local, remote)["customTasks"]}
        self.assertEqual(set(tasks), {"a", "b", "c"})
        self.assertEqual(tasks["a"], {"id": "a", "title": "PC rename", "notes": "phone"})

    def test_missing_base_arrays_and_maps_preserve_concurrent_new_items(self):
        merged = tasks_pc.merge_state({}, {"customTasks": [{"id": "phone"}], "manualLinks": {"a": ["b"]}},
                                      {"customTasks": [{"id": "pc"}], "manualLinks": {"a": ["c"], "c": ["a"]}})
        self.assertEqual({x["id"] for x in merged["customTasks"]}, {"phone", "pc"})
        self.assertEqual(set(merged["manualLinks"]["a"]), {"b", "c"})
        self.assertEqual(merged["manualLinks"]["c"], ["a"])

    def test_manual_links_apply_peer_additions_and_removals(self):
        base = {"manualLinks": {"a": ["b"], "b": ["a"]}}
        local = {"manualLinks": {"a": ["d"], "b": [], "d": ["a"]}}
        remote = {"manualLinks": {"a": ["b", "c"], "b": ["a"], "c": ["a"]}}
        merged = tasks_pc.merge_state(base, local, remote)["manualLinks"]
        self.assertEqual(set(merged["a"]), {"c", "d"})
        self.assertEqual(merged["b"], [])
        self.assertEqual(merged["c"], ["a"])

    def test_task_and_nested_key_removals_are_preserved(self):
        base = {"customTasks": [{"id": "a"}, {"id": "b"}], "taskOverrides": {"a": {"notes": "gone", "priority": "LOW"}}}
        local = {"customTasks": [{"id": "b"}], "taskOverrides": {"a": {"priority": "HIGH"}}}
        remote = {"customTasks": [{"id": "a"}, {"id": "b"}, {"id": "remote"}], "taskOverrides": {"a": {"notes": "gone", "priority": "LOW", "time": "9:00"}}}
        merged = tasks_pc.merge_state(base, local, remote)
        self.assertEqual({x["id"] for x in merged["customTasks"]}, {"b", "remote"})
        self.assertEqual(merged["taskOverrides"]["a"], {"priority": "HIGH", "time": "9:00"})

    def test_concurrent_progress_keeps_unrelated_tasks_and_applies_group_stop(self):
        base={"inProgressTasks":["a","b"],"inProgress":"b"}
        local={"inProgressTasks":[],"inProgress":None}
        remote={"inProgressTasks":["a","b","other"],"inProgress":"other"}
        result=tasks_pc.merge_state(base,local,remote)
        self.assertEqual(result["inProgressTasks"],["other"])
        self.assertEqual(result["inProgress"],"other")

    def test_device_filters_and_volatile_update_time_are_not_synced(self):
        self.assertEqual(tasks_pc.merge_state({"uiContext": "Home"}, {"uiContext": "Phone", "updatedAt": "now", "completed": {}},
                                             {"uiContext": "PC", "updatedAt": "old", "theme": "keep"}), {"theme": "keep", "completed": {}})


class PersistenceTests(unittest.TestCase):
    def test_saved_state_survives_restart_and_revision_only_changes_with_content(self):
        with tempfile.TemporaryDirectory() as folder:
            store = tasks_pc.StateStore(folder, {"completed": {"a": False}})
            initial = store.snapshot()
            result = store.sync(initial["state"], {"completed": {"a": True}})
            reopened = tasks_pc.StateStore(folder, {"completed": {"wrong": True}})
            self.assertEqual(reopened.snapshot(), result)
            self.assertEqual(reopened.sync(result["state"], result["state"]), result)

    def test_failed_disk_write_does_not_change_live_state(self):
        with tempfile.TemporaryDirectory() as folder:
            store = tasks_pc.StateStore(folder, {"completed": {"a": False}})
            before = store.snapshot()
            with patch.object(tasks_pc, "atomic_json", side_effect=OSError("disk full")):
                with self.assertRaises(OSError):
                    store.sync(before["state"], {"completed": {"a": True}})
            self.assertEqual(store.snapshot(), before)
            self.assertEqual(tasks_pc.StateStore(folder, {}).snapshot(), before)

    def test_first_phone_import_is_authoritative_and_pairing_survives_restart(self):
        with tempfile.TemporaryDirectory() as folder:
            store=tasks_pc.StateStore(folder,{"customTasks":[{"id":"old","title":"Old bundled task"}]})
            result=store.sync({}, {"customTasks":[{"id":"phone","title":"Current phone"}],"completed":{}}, phone=True)
            self.assertTrue(result["phoneLinked"])
            self.assertGreater(result["phoneLastSeenAt"],0)
            self.assertEqual([task["id"] for task in result["state"]["customTasks"]],["phone"])
            self.assertEqual(tasks_pc.StateStore(folder,{}).snapshot(),result)

    def test_damaged_existing_state_is_never_replaced_by_seed(self):
        with tempfile.TemporaryDirectory() as folder:
            target = Path(folder) / "tasks-state.json"
            target.write_text("damaged", encoding="utf-8")
            with self.assertRaises(ValueError):
                tasks_pc.StateStore(folder, {"new": True})
            self.assertEqual(target.read_text(), "damaged")


class FakeSocket:
    def __init__(self, request):
        self.reader = io.BytesIO(request)
        self.response = io.BytesIO()
    def makefile(self, mode, *args):
        return self.reader
    def sendall(self, data):
        self.response.write(data)


class EndpointTests(unittest.TestCase):
    def setUp(self):
        self.folder = tempfile.TemporaryDirectory()
        self.store = tasks_pc.StateStore(self.folder.name, {"completed": {"a": False}})
        assets = Path(self.folder.name) / "assets"
        assets.mkdir()
        (assets / "index.html").write_text("<html><head></head><body>Tasks</body></html>")
        self.desktop = Path(__file__).resolve().parents[1]
        self.server = types.SimpleNamespace(store=self.store, token="f" * 64, assets=assets, desktop=self.desktop,
            pairing_url="http://192.168.1.20:8765/pair#" + "f" * 64, lan_origin="http://192.168.1.20:8765", server_port=8765)

    def tearDown(self):
        self.folder.cleanup()

    def request(self, path, method="GET", value=None, auth=True, address="192.168.1.10", length=None, host=None):
        payload = tasks_pc.encode_json(value) if value is not None else b""
        headers = [f"{method} {path} HTTP/1.1", "Host: " + (host or ("127.0.0.1:8765" if address == "127.0.0.1" else "192.168.1.20:8765")), "Connection: close"]
        if auth:
            headers.append("Authorization: Bearer " + self.server.token)
        if value is not None or length is not None:
            headers.append("Content-Length: " + str(len(payload) if length is None else length))
        sock = FakeSocket(("\r\n".join(headers) + "\r\n\r\n").encode() + payload)
        tasks_pc.TasksHandler(sock, (address, 54321), self.server)
        raw_headers, body = sock.response.getvalue().split(b"\r\n\r\n", 1)
        return int(raw_headers.split()[1]), body

    def test_no_state_or_pairing_secrets_without_bearer(self):
        for path in ("/api/state", "/api/pair"):
            status, body = self.request(path, auth=False)
            self.assertEqual(status, 401)
            self.assertNotIn(self.server.token.encode(), body)
            self.assertNotIn(b'"completed"', body)
        status, body = self.request("/api/sync", "POST", {"base": {}, "state": {"attack": True}}, auth=False)
        self.assertEqual(status, 401)
        self.assertNotIn("attack", self.store.snapshot()["state"])

    def test_pair_landing_contains_no_embedded_token_and_main_is_loopback_only(self):
        status, body = self.request("/pair", auth=False)
        self.assertEqual(status, 200)
        self.assertNotIn(self.server.token.encode(), body)
        status, _ = self.request("/", auth=False)
        self.assertEqual(status, 403)
        status, body = self.request("/", auth=False, address="127.0.0.1")
        self.assertEqual(status, 200)
        self.assertIn(b"__TASKS_DESKTOP_BOOT__", body)

    def test_rebound_host_cannot_fetch_desktop_pairing_secret(self):
        status, body = self.request("/", auth=False, address="127.0.0.1", host="untrusted.example:8765")
        self.assertEqual(status, 403)
        self.assertNotIn(self.server.token.encode(), body)

    def test_authorized_sync_merges_and_survives_restart(self):
        status, body = self.request("/api/state")
        before = json.loads(body)["state"]
        self.assertEqual(status, 200)
        status, body = self.request("/api/sync", "POST", {"base": before, "state": {"completed": {"a": True}}})
        self.assertEqual(status, 200)
        self.assertTrue(json.loads(body)["state"]["completed"]["a"])
        self.assertTrue(tasks_pc.StateStore(self.folder.name, {}).snapshot()["state"]["completed"]["a"])

    def test_oversized_or_invalid_request_cannot_replace_data(self):
        before = self.store.snapshot()
        status, _ = self.request("/api/sync", "POST", {}, length=tasks_pc.MAX_BYTES + 1)
        self.assertEqual(status, 413)
        status, _ = self.request("/api/sync", "POST", {"base": [], "state": {}})
        self.assertEqual(status, 400)
        self.assertEqual(self.store.snapshot(), before)

    @unittest.skipUnless(importlib.util.find_spec("qrcode"), "qrcode is installed by desktop CI")
    def test_pair_endpoint_emits_a_qr_svg_only_with_authorization(self):
        status, body = self.request("/api/pair")
        self.assertEqual(status, 200)
        pair = json.loads(body)
        self.assertEqual(pair["url"], self.server.pairing_url)
        self.assertIn("<svg", pair["qrSvg"])


if __name__ == "__main__":
    unittest.main()

"""Tasks PC: a small local desktop companion using the system browser, without a cloud account."""
from __future__ import annotations

import argparse
import copy
import hmac
import ipaddress
import json
import os
from pathlib import Path
import secrets
import socket
import subprocess
import sys
import tempfile
import time
import threading
import urllib.error
import urllib.parse
import urllib.request
import webbrowser
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

MAX_BYTES = 2 * 1024 * 1024
LOCAL_FIELDS = {"uiContext", "updatedAt"}
MISSING = object()


def clone(value):
    return copy.deepcopy(value)


def _task_array(value):
    return isinstance(value, list) and all(isinstance(item, dict) and isinstance(item.get("id"), str) for item in value)


def merge_changed(base, local, remote, path=()):
    """Apply local changes since base over remote; unrelated remote changes survive.

    Map removals are deliberate. Tasks merge by stable id instead of list position.
    Manual-link lists apply set changes so different peers can add unrelated links.
    Conflicting edits to the same scalar use the most recently received request.
    """
    if base is not MISSING and local is not MISSING and local == base:
        return clone(remote) if remote is not MISSING else MISSING
    if local is MISSING:
        return MISSING if base is not MISSING else (clone(remote) if remote is not MISSING else MISSING)
    if isinstance(local, dict) and (base is MISSING or isinstance(base, dict)):
        base_dict = {} if base is MISSING else base
        result = clone(remote) if isinstance(remote, dict) else {}
        for key in set(base_dict) | set(local):
            if not path and key in LOCAL_FIELDS:
                continue
            value = merge_changed(base_dict.get(key, MISSING), local.get(key, MISSING), result.get(key, MISSING), path + (key,))
            if value is MISSING:
                result.pop(key, None)
            else:
                result[key] = value
        return result
    if path == ("customTasks",) and _task_array(local) and (base is MISSING or _task_array(base)):
        base_map = {task["id"]: task for task in ([] if base is MISSING else base)}
        local_map = {task["id"]: task for task in local}
        remote_tasks = remote if _task_array(remote) else []
        result = {task["id"]: clone(task) for task in remote_tasks}
        order = [task["id"] for task in remote_tasks]
        for task_id in list(base_map) + [key for key in local_map if key not in base_map]:
            value = merge_changed(base_map.get(task_id, MISSING), local_map.get(task_id, MISSING), result.get(task_id, MISSING), path + (task_id,))
            if value is MISSING:
                result.pop(task_id, None)
            else:
                result[task_id] = value
                if task_id not in order:
                    order.append(task_id)
        return [result[task_id] for task_id in order if task_id in result]
    if (path == ("inProgressTasks",) or len(path) == 2 and path[0] == "manualLinks") and isinstance(local, list) and all(isinstance(x, str) for x in local):
        original = base if isinstance(base, list) else []
        current = remote if isinstance(remote, list) else []
        removed = set(original) - set(local)
        return list(dict.fromkeys([peer for peer in current if peer not in removed] + [peer for peer in local if peer not in original]))
    return clone(local)


def merge_state(base, local, remote):
    if not all(isinstance(value, dict) for value in (base, local, remote)):
        raise ValueError("Tasks state must be a JSON object")
    merged = merge_changed(base, local, remote)
    if isinstance(merged.get("inProgressTasks"), list):
        merged["inProgress"] = next(iter(reversed(merged["inProgressTasks"])), None)
    # UI filters and timestamps belong to clients; these must not create sync churn.
    for key in LOCAL_FIELDS:
        merged.pop(key, None)
    encoded = encode_json(merged)
    if len(encoded) > MAX_BYTES:
        raise ValueError("Tasks state is too large")
    return merged


def encode_json(value):
    return json.dumps(value, ensure_ascii=False, separators=(",", ":"), allow_nan=False).encode("utf-8")


def parse_json(data):
    def reject_constant(value):
        raise ValueError("JSON must not contain non-finite numbers")
    return json.loads(data, parse_constant=reject_constant)


def atomic_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    payload = encode_json(value)
    with tempfile.NamedTemporaryFile(dir=path.parent, prefix=path.name + ".", delete=False) as stream:
        temporary = Path(stream.name)
        try:
            stream.write(payload)
            stream.flush()
            os.fsync(stream.fileno())
        except BaseException:
            temporary.unlink(missing_ok=True)
            raise
    try:
        os.replace(temporary, path)
    finally:
        temporary.unlink(missing_ok=True)


class StateStore:
    def __init__(self, directory, seed):
        self.directory = Path(directory)
        self.path = self.directory / "tasks-state.json"
        self.lock = threading.RLock()
        if self.path.exists():
            value = parse_json(self.path.read_bytes())
            if not isinstance(value, dict) or not isinstance(value.get("state"), dict) or not isinstance(value.get("revision"), int):
                raise ValueError("The saved Tasks data could not be read. It has not been replaced.")
            self.state = value["state"]
            self.revision = value["revision"]
            self.phone_linked = value.get("phoneLinked", False)
            self.phone_seen = value.get("phoneLastSeenAt", 0)
        else:
            self.state = merge_state({}, seed, {})
            self.revision = 1
            self.phone_linked = False
            self.phone_seen = 0
            atomic_json(self.path, self.snapshot())

    def snapshot(self):
        with self.lock:
            return {"state": clone(self.state), "revision": self.revision, "phoneLinked":self.phone_linked, "phoneLastSeenAt":self.phone_seen}

    def sync(self, base, local, phone=False):
        with self.lock:
            # First pairing imports the current phone, never the bundled historical seed.
            merged = merge_state({} if phone and not self.phone_linked else base, local,
                                 {} if phone and not self.phone_linked else self.state)
            seen = time.time() if phone else self.phone_seen
            linked = self.phone_linked or phone
            changed = merged != self.state or linked != self.phone_linked
            next_revision = self.revision + (1 if changed else 0)
            if changed or phone:
                atomic_json(self.path, {"state":merged, "revision":next_revision,
                                       "phoneLinked":linked, "phoneLastSeenAt":seen})
                self.state, self.revision = merged, next_revision
                self.phone_linked, self.phone_seen = linked, seen
            return self.snapshot()


def resources():
    if getattr(sys, "frozen", False):
        root = Path(sys._MEIPASS)
        return root / "app-assets/www", root / "desktop"
    here = Path(__file__).resolve().parent
    portable = here / "assets"
    return (portable if portable.exists() else here.parent / "app/src/main/assets/www"), here


def data_directory():
    return Path(os.environ.get("TASKS_DATA_DIR") or (Path(os.environ.get("LOCALAPPDATA", Path.home() / ".local/share")) / "Tasks"))


def private_addresses():
    candidates = []
    try:
        with socket.socket(socket.AF_INET, socket.SOCK_DGRAM) as probe:
            probe.connect(("192.0.2.1", 9))  # Route lookup only: no packet is sent.
            candidates.append(probe.getsockname()[0])
    except OSError:
        pass
    try:
        candidates.extend(item[4][0] for item in socket.getaddrinfo(socket.gethostname(), None, socket.AF_INET))
    except OSError:
        pass
    result = []
    for candidate in candidates:
        try:
            address = ipaddress.IPv4Address(candidate)
            if any(address in network for network in (ipaddress.ip_network("10.0.0.0/8"), ipaddress.ip_network("172.16.0.0/12"), ipaddress.ip_network("192.168.0.0/16"))):
                if candidate not in result:
                    result.append(candidate)
        except ValueError:
            pass
    return result or ["127.0.0.1"]


class TasksServer(ThreadingHTTPServer):
    daemon_threads = True
    allow_reuse_address = True

    def __init__(self, address, store, token, assets, desktop, lan_host=None):
        self.store, self.token = store, token
        self.assets, self.desktop = Path(assets), Path(desktop)
        self.lan_host = lan_host or private_addresses()[0]
        self.stop_event = threading.Event()
        super().__init__(address, TasksHandler)

    @property
    def lan_origin(self):
        return f"http://{self.lan_host}:{self.server_port}"

    @property
    def pairing_url(self):
        return self.lan_origin + "/pair#" + self.token


class TasksHandler(BaseHTTPRequestHandler):
    server_version = "Tasks"
    sys_version = ""

    def log_message(self, fmt, *args):
        pass  # Tasks content and pairing secrets never enter a request log.

    def loopback(self):
        try:
            return ipaddress.ip_address(self.client_address[0]).is_loopback
        except ValueError:
            return False

    def local_host(self):
        try:
            host = urllib.parse.urlsplit("http://" + self.headers.get("Host", "")).hostname
            return host in {"127.0.0.1", "localhost", "::1"}
        except ValueError:
            return False

    def authorized(self):
        supplied = self.headers.get("Authorization", "")
        return hmac.compare_digest(supplied.encode("utf-8"), ("Bearer " + self.server.token).encode("utf-8"))

    def respond(self, status, data, content_type="application/json; charset=utf-8"):
        if not isinstance(data, bytes):
            data = encode_json(data)
        self.send_response(status)
        self.send_header("Content-Type", content_type)
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "no-store")
        self.send_header("X-Content-Type-Options", "nosniff")
        self.send_header("X-Tasks-Companion", "1")
        self.send_header("Referrer-Policy", "no-referrer")
        self.send_header("X-Frame-Options", "DENY")
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        path = urllib.parse.urlsplit(self.path).path
        if path == "/pair":
            self.respond(200, (self.server.desktop / "pair.html").read_bytes(), "text/html; charset=utf-8")
            return
        if path.startswith("/api/"):
            if not self.authorized():
                self.respond(401, {"error": "Pair Tasks to this PC first"})
                return
            if path == "/api/state":
                self.respond(200, self.server.store.snapshot())
            elif path == "/api/pair":
                import io
                import qrcode
                import qrcode.image.svg
                svg = qrcode.make(self.server.pairing_url, image_factory=qrcode.image.svg.SvgPathImage, box_size=7, border=3)
                image = io.BytesIO()
                svg.save(image)
                self.respond(200, {"url": self.server.pairing_url, "origin": self.server.lan_origin,
                                   "qrSvg": image.getvalue().decode("utf-8"), "addresses": private_addresses()})
            else:
                self.respond(404, {"error": "Not found"})
            return
        # Only this computer can load a page containing the desktop bridge credentials.
        if not self.loopback() or not self.local_host():
            self.respond(403, {"error": "Open Tasks on this computer"})
            return
        if path in ("/", "/index.html"):
            boot = self.server.store.snapshot()
            boot.update(token=self.server.token, origin=f"http://127.0.0.1:{self.server.server_port}", pairingUrl=f"http://127.0.0.1:{self.server.server_port}/pair#{self.server.token}")
            script = "<script>window.__TASKS_DESKTOP_BOOT__=" + encode_json(boot).decode("utf-8").replace("<", "\\u003c") + ";</script>"
            bridge = "<script>" + (self.server.desktop / "desktop-bridge.js").read_text("utf-8").replace("</script", "<\\/script") + "</script>"
            html = (self.server.assets / "index.html").read_text("utf-8")
            html = html.replace("<head>", "<head>" + script + bridge, 1)
            # Some packaged documents put attributes/metadata directly on their first head tag.
            if "window.__TASKS_DESKTOP_BOOT__=" not in html:
                html = script + bridge + html
            self.respond(200, html.encode("utf-8"), "text/html; charset=utf-8")
        elif path == "/favicon.ico":
            icon = self.server.desktop / "tasks.ico"
            if icon.exists():
                self.respond(200, icon.read_bytes(), "image/x-icon")
            else:
                self.respond(404, {"error": "Not found"})
        else:
            filename = urllib.parse.unquote(path).lstrip("/")
            allowed = {".js": "application/javascript; charset=utf-8", ".css": "text/css; charset=utf-8", ".json": "application/json; charset=utf-8", ".svg": "image/svg+xml", ".png": "image/png"}
            candidate = None
            for root in (self.server.assets, self.server.assets.parent):
                target = (root / filename).resolve()
                if target.is_relative_to(root.resolve()) and target.is_file() and target.suffix in allowed:
                    candidate = target
                    break
            if candidate is None:
                self.respond(404, {"error": "Not found"})
            else:
                self.respond(200, candidate.read_bytes(), allowed[candidate.suffix])

    def do_POST(self):
        path = urllib.parse.urlsplit(self.path).path
        if not self.authorized():
            self.respond(401, {"error": "Pair Tasks to this PC first"})
            return
        if path == "/api/quit":
            if not self.loopback():
                self.respond(403, {"error": "Close the companion on the PC"})
                return
            self.respond(200, {"stopping": True})
            self.server.stop_event.set()
            threading.Thread(target=self.server.shutdown, daemon=True).start()
            return
        if path != "/api/sync":
            self.respond(404, {"error": "Not found"})
            return
        try:
            length = int(self.headers.get("Content-Length", "-1"))
            if not 0 <= length <= MAX_BYTES:
                self.respond(413, {"error": "Tasks update is too large"})
                return
            # A full base and next state are bounded together to 2 MiB.
            data = parse_json(self.rfile.read(length))
            if not isinstance(data, dict) or not isinstance(data.get("base"), dict) or not isinstance(data.get("state"), dict):
                raise ValueError("Expected base and state objects")
            result = self.server.store.sync(data["base"], data["state"], phone=self.headers.get("X-Tasks-Client") == "android")
            self.respond(200, result)
        except (ValueError, UnicodeError, json.JSONDecodeError):
            self.respond(400, {"error": "Tasks update could not be read"})
        except OSError:
            self.respond(503, {"error": "Could not save Tasks. Existing data is unchanged."})


def load_configuration(directory):
    path = directory / "companion.json"
    if path.exists():
        value = parse_json(path.read_bytes())
        if not isinstance(value, dict) or not isinstance(value.get("token"), str) or len(value["token"]) != 64 or any(c not in "0123456789abcdef" for c in value["token"]):
            raise ValueError("The Tasks pairing configuration is invalid. It has not been replaced.")
        return value
    value = {"token": secrets.token_hex(32), "port": 8765, "ready": False}
    atomic_json(path, value)
    return value


def existing_companion(port, token):
    request = urllib.request.Request(f"http://127.0.0.1:{port}/api/state", headers={"Authorization": "Bearer " + token})
    try:
        with urllib.request.urlopen(request, timeout=.6) as response:
            value = parse_json(response.read(MAX_BYTES))
            return response.status == 200 and response.headers.get("X-Tasks-Companion") == "1" and isinstance(value, dict) and isinstance(value.get("state"), dict)
    except (OSError, ValueError):
        return False


def open_window(url, directory):
    if os.name == "nt":
        for root in (os.environ.get("PROGRAMFILES(X86)"), os.environ.get("PROGRAMFILES"), os.environ.get("LOCALAPPDATA")):
            if not root:
                continue
            edge = Path(root) / "Microsoft/Edge/Application/msedge.exe"
            if edge.is_file():
                try:
                    return subprocess.Popen([str(edge), "--app=" + url, "--user-data-dir=" + str(directory / "Browser"),
                                             "--no-first-run", "--no-default-browser-check", "--disable-background-mode", "--window-size=560,900"])
                except OSError:
                    pass
    webbrowser.open(url)
    return None


def notify_error(message):
    if os.name == "nt":
        import ctypes
        ctypes.windll.user32.MessageBoxW(None, message, "Tasks", 0x10)
    else:
        print(message, file=sys.stderr)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--no-browser", action="store_true", help="Run the local server without opening a window")
    parser.add_argument("--port", type=int)
    parser.add_argument("--host", default="0.0.0.0")
    args = parser.parse_args()
    try:
        assets, desktop = resources()
        directory = data_directory()
        config = load_configuration(directory)
        port = args.port if args.port is not None else int(config.get("port", 8765))
        if existing_companion(port, config["token"]):
            if not args.no_browser:
                open_window(f"http://127.0.0.1:{port}/", directory)
            return 0
        seed = {}  # Wait for the phone instead of showing an old bundled task snapshot.
        store = StateStore(directory, seed)
        try:
            server = TasksServer((args.host, port), store, config["token"], assets, desktop)
        except OSError:
            server = TasksServer((args.host, 0), store, config["token"], assets, desktop)
        config["port"] = server.server_port
        config["ready"] = True
        atomic_json(directory / "companion.json", config)
        worker = threading.Thread(target=server.serve_forever, daemon=True)
        worker.start()
        if not args.no_browser:
            open_window(f"http://127.0.0.1:{server.server_port}/", directory)
        # Reopening the executable reuses this server. Quit is available on the pairing screen.
        while not server.stop_event.wait(.5):
            if not worker.is_alive():
                break
        server.server_close()
        return 0
    except KeyboardInterrupt:
        return 0
    except (OSError, ValueError) as error:
        notify_error(str(error))
        return 1


if __name__ == "__main__":
    raise SystemExit(main())

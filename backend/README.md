# FlowCast FastAPI Backend

This backend serves FlowCast Demo data with a mock fallback. In `hybrid` mode it
uses Pexels for video search and GNews for news when API keys are configured;
otherwise it keeps returning local mock data so the Android prototype remains
usable offline.

## Run

```bash
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
uvicorn app.main:app --reload
```

Open:

```text
http://127.0.0.1:8000/docs
```

For a physical Android phone on the same network, start the backend on all
interfaces and build the app with the computer LAN IP:

```bash
cd backend
.venv/bin/uvicorn app.main:app --host 0.0.0.0 --port 8000
```

```properties
# android/gradle.properties
FLOWCAST_BASE_URL=http://YOUR_COMPUTER_LAN_IP:8000
```

The emulator-only address `http://10.0.2.2:8000` does not work from a real
phone.

## Public data configuration

Environment variables:

```bash
export FLOWCAST_DATA_MODE=hybrid  # mock | public | hybrid, default hybrid
export PEXELS_API_KEY=your_pexels_key
export GNEWS_API_KEY=your_gnews_key
export PUBLIC_API_TIMEOUT_SECONDS=5
export PUBLIC_API_CACHE_TTL_SECONDS=300
```

Modes:

- `mock`: never calls public providers.
- `public`: tries public providers when keys exist, and falls back to mock data
  on missing keys, empty provider payloads, timeouts, or provider errors.
- `hybrid`: default; same provider behavior as `public`, with mock data as the
  stable local fallback.

Apply for keys:

- Pexels: create a developer account at `https://www.pexels.com/api/`, generate
  an API key, then set `PEXELS_API_KEY`. The backend sends it with the
  `Authorization` header to `https://api.pexels.com/v1/videos/search`.
- GNews: create a token at `https://gnews.io/`, then set `GNEWS_API_KEY`. The
  backend sends it as the `apikey` query parameter to GNews `top-headlines` or
  `search`.

## Endpoints

```text
GET  /health
GET  /videos/feed?cursor=1&limit=20&query=technology
GET  /news?channel=ai&cursor=1&limit=20
GET  /news/{id}
POST /orders
GET  /orders/{id}
GET  /payments/methods
GET  /payments/status/{order_id}
POST /payments/mock
GET  /nfc/scenarios
```

`/videos/feed` and `/news` return:

```json
{
  "items": [],
  "next_cursor": null,
  "has_more": false
}
```

Errors are normalized as:

```json
{
  "error": {
    "code": "ORDER_NOT_FOUND",
    "message": "Order not found",
    "details": {}
  }
}
```

The fallback mock payloads intentionally include enough display and routing data
for the Android prototype to demonstrate the full PRD flow: vertical video
cards, AI news summaries, cashier/payment states, and three NFC Scheme
scenarios.

## Test

```bash
source .venv/bin/activate
pip install -r requirements.txt
pytest
```

The tests use FastAPI `TestClient`, monkeypatch public providers, and do not
require real external network access.

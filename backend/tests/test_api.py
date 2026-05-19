import httpx
from fastapi.testclient import TestClient

import app.main as main
from app.main import app


client = TestClient(app)


def test_health_defaults_to_hybrid(monkeypatch) -> None:
    monkeypatch.delenv("FLOWCAST_DATA_MODE", raising=False)
    monkeypatch.delenv("PEXELS_API_KEY", raising=False)
    monkeypatch.delenv("GNEWS_API_KEY", raising=False)

    response = client.get("/health")

    assert response.status_code == 200
    payload = response.json()
    assert payload["mode"] == "hybrid"


def test_health(monkeypatch) -> None:
    monkeypatch.delenv("FLOWCAST_DATA_MODE", raising=False)
    monkeypatch.delenv("PEXELS_API_KEY", raising=False)
    monkeypatch.delenv("GNEWS_API_KEY", raising=False)

    response = client.get("/health")

    assert response.status_code == 200
    payload = response.json()
    assert payload["status"] == "ok"
    assert payload["service"] == "flowcast-demo-api"
    assert payload["version"] == app.version
    assert payload["mode"] == "hybrid"
    assert payload["timestamp"].endswith("+00:00")


def test_videos_feed_returns_mock_fallback_items(monkeypatch) -> None:
    monkeypatch.setenv("FLOWCAST_DATA_MODE", "mock")

    response = client.get("/videos/feed", params={"limit": 2})

    assert response.status_code == 200
    payload = response.json()
    assert payload["next_cursor"] == "2"
    assert payload["has_more"] is True
    assert len(payload["items"]) == 2
    for item in payload["items"]:
        assert item["id"].startswith("v")
        assert item["source"] == "Public Video API placeholder"
        assert item["cover_url"].startswith("https://")
        assert item["play_url"].startswith("https://")
        assert item["duration_seconds"] > 0
        assert item["tags"]


def test_videos_feed_maps_pexels_provider(monkeypatch) -> None:
    main._PUBLIC_CACHE.clear()
    monkeypatch.setenv("FLOWCAST_DATA_MODE", "public")
    monkeypatch.setenv("PEXELS_API_KEY", "pexels-test-key")

    def fake_get(url, params=None, headers=None, timeout=None, trust_env=None):
        assert url == main.PEXELS_VIDEO_SEARCH_URL
        assert params == {"query": "ai", "page": 1, "per_page": 1}
        assert headers == {"Authorization": "pexels-test-key"}
        assert timeout == 5.0
        assert trust_env is False
        return httpx.Response(
            200,
            json={
                "total_results": 2,
                "videos": [
                    {
                        "id": 123,
                        "url": "https://www.pexels.com/video/robot-cafe-123/",
                        "image": "https://images.pexels.com/video-cover.jpg",
                        "duration": 12,
                        "likes": 1350,
                        "user": {"name": "Pexels Maker"},
                        "video_files": [
                            {"width": 640, "height": 360, "link": "https://video.low.mp4"},
                            {"width": 1920, "height": 1080, "link": "https://video.high.mp4"},
                        ],
                    }
                ],
            },
        )

    monkeypatch.setattr(main.httpx, "get", fake_get)

    response = client.get("/videos/feed", params={"query": "ai", "limit": 1})

    assert response.status_code == 200
    payload = response.json()
    assert payload["has_more"] is True
    assert payload["next_cursor"] == "2"
    item = payload["items"][0]
    assert item["id"] == "pexels-123"
    assert item["title"] == "Robot Cafe 123"
    assert item["author"] == "Pexels Maker"
    assert item["source"] == "Pexels"
    assert item["play_url"] == "https://video.high.mp4"
    assert item["likes"] == "1.4k"


def test_videos_feed_provider_failure_falls_back_to_mock(monkeypatch) -> None:
    main._PUBLIC_CACHE.clear()
    monkeypatch.setenv("FLOWCAST_DATA_MODE", "public")
    monkeypatch.setenv("PEXELS_API_KEY", "pexels-test-key")

    def fake_get(*args, **kwargs):
        raise httpx.ConnectError("network down")

    monkeypatch.setattr(main.httpx, "get", fake_get)

    response = client.get("/videos/feed", params={"limit": 1})

    assert response.status_code == 200
    payload = response.json()
    assert payload["items"][0]["id"] == "v1"
    assert payload["items"][0]["source"] == "Public Video API placeholder"


def test_news_list_matches_summary_contract() -> None:
    response = client.get("/news", params={"limit": 1})

    assert response.status_code == 200
    payload = response.json()
    assert payload["next_cursor"] == "2"
    assert payload["has_more"] is True
    assert len(payload["items"]) == 1
    for item in payload["items"]:
        assert item["id"].startswith("n")
        assert item["title"]
        assert item["summary"]
        assert item["source"]
        assert item["published_at"]


def test_news_channel_filters_mock_items(monkeypatch) -> None:
    monkeypatch.setenv("FLOWCAST_DATA_MODE", "mock")

    response = client.get("/news", params={"channel": "AI", "limit": 10})

    assert response.status_code == 200
    payload = response.json()
    assert payload["items"]
    assert all("AI" in item["title"] or "AI" in item["summary"] or "AI" in item["source"] for item in payload["items"])


def test_news_maps_gnews_top_headlines(monkeypatch) -> None:
    main._PUBLIC_CACHE.clear()
    monkeypatch.setenv("FLOWCAST_DATA_MODE", "public")
    monkeypatch.setenv("GNEWS_API_KEY", "gnews-test-key")

    def fake_get(url, params=None, timeout=None, trust_env=None):
        assert url == "https://gnews.io/api/v4/top-headlines"
        assert params["apikey"] == "gnews-test-key"
        assert params["category"] == "technology"
        assert params["max"] == 1
        assert params["page"] == 1
        assert timeout == 5.0
        assert trust_env is False
        return httpx.Response(
            200,
            json={
                "totalArticles": 2,
                "articles": [
                    {
                        "title": "AI tools reach enterprise teams",
                        "description": "Teams are adopting AI workbenches.",
                        "content": "Long content",
                        "url": "https://news.example/ai-tools",
                        "publishedAt": "2026-05-18T01:00:00Z",
                        "source": {"name": "Example News", "url": "https://news.example"},
                    }
                ],
            },
        )

    monkeypatch.setattr(main.httpx, "get", fake_get)

    response = client.get("/news", params={"channel": "ai", "limit": 1})

    assert response.status_code == 200
    payload = response.json()
    assert payload["has_more"] is True
    assert payload["next_cursor"] == "2"
    item = payload["items"][0]
    assert item["title"] == "AI tools reach enterprise teams"
    assert item["summary"] == "Teams are adopting AI workbenches."
    assert item["source"] == "Example News"
    assert item["source_url"] == "https://news.example/ai-tools"


def test_news_maps_gnews_search_for_custom_channel(monkeypatch) -> None:
    main._PUBLIC_CACHE.clear()
    monkeypatch.setenv("FLOWCAST_DATA_MODE", "public")
    monkeypatch.setenv("GNEWS_API_KEY", "gnews-test-key")

    def fake_get(url, params=None, timeout=None, trust_env=None):
        assert url == "https://gnews.io/api/v4/search"
        assert params["q"] == "fintech"
        assert trust_env is False
        return httpx.Response(200, json={"totalArticles": 1, "articles": []})

    monkeypatch.setattr(main.httpx, "get", fake_get)

    response = client.get("/news", params={"channel": "fintech"})

    assert response.status_code == 200
    assert response.json()["items"][0]["id"] == "n1"


def test_news_provider_failure_falls_back_to_mock(monkeypatch) -> None:
    main._PUBLIC_CACHE.clear()
    monkeypatch.setenv("FLOWCAST_DATA_MODE", "public")
    monkeypatch.setenv("GNEWS_API_KEY", "gnews-test-key")

    def fake_get(*args, **kwargs):
        raise httpx.TimeoutException("timeout")

    monkeypatch.setattr(main.httpx, "get", fake_get)

    response = client.get("/news", params={"limit": 1})

    assert response.status_code == 200
    assert response.json()["items"][0]["id"] == "n1"


def test_news_detail() -> None:
    response = client.get("/news/n1")

    assert response.status_code == 200
    payload = response.json()
    assert payload["id"] == "n1"
    assert payload["key_points"]
    assert payload["source_url"].startswith("https://")


def test_news_detail_404_for_unknown_id() -> None:
    response = client.get("/news/missing")

    assert response.status_code == 404
    assert response.json() == {
        "error": {
            "code": "NEWS_NOT_FOUND",
            "message": "News item not found",
        }
    }


def test_create_order_uses_defaults_and_cashier_scheme() -> None:
    response = client.post("/orders", json={})

    assert response.status_code == 200
    payload = response.json()
    assert payload["merchant_id"] == "M10001"
    assert payload["merchant_name"] == "FlowCast 线下门店"
    assert payload["amount"] == 88.0
    assert payload["status"] == "pending"
    assert payload["scene"] == "demo_cashier"
    assert payload["payment_method"] is None
    assert payload["cashier_scheme"].startswith(
        f"flowcast://pay/cashier?orderId={payload['id']}&amount=88.00"
    )

    get_response = client.get(f"/orders/{payload['id']}")
    assert get_response.status_code == 200
    assert get_response.json() == payload


def test_create_order_rejects_invalid_amount() -> None:
    response = client.post("/orders", json={"amount": -1})

    assert response.status_code == 422
    payload = response.json()
    assert payload["error"]["code"] == "INVALID_REQUEST"
    assert payload["error"]["message"] == "请求参数验证失败"
    assert payload["error"]["details"]["errors"]


def test_create_order_and_update_payment_status() -> None:
    order_response = client.post(
        "/orders",
        json={
            "merchant_id": "M20001",
            "amount": 66.6,
            "scene": "demo_test",
            "payment_method": "alipay",
        },
    )

    assert order_response.status_code == 200
    order = order_response.json()
    assert order["status"] == "pending"
    assert order["scene"] == "demo_test"
    assert order["cashier_scheme"].startswith("flowcast://pay/cashier")

    payment_response = client.post(
        "/payments/mock",
        json={
            "order_id": order["id"],
            "method": "wechat",
            "status": "success",
        },
    )

    assert payment_response.status_code == 200
    paid_order = payment_response.json()
    assert paid_order["status"] == "success"
    assert paid_order["payment_method"] == "wechat"
    assert paid_order["cashier_scheme"].startswith(
        f"flowcast://pay/cashier?orderId={order['id']}"
    )

    status_response = client.get(f"/payments/status/{order['id']}")
    assert status_response.status_code == 200
    assert status_response.json() == {
        "order_id": order["id"],
        "status": "success",
        "method": "wechat",
        "amount": 66.6,
    }


def test_payment_methods_match_supported_mock_schemes() -> None:
    response = client.get("/payments/methods")

    assert response.status_code == 200
    items = response.json()["items"]
    assert [item["id"] for item in items] == ["alipay", "wechat", "unionpay"]
    for item in items:
        assert item["enabled"] is True
        assert item["label"]
        assert item["description"]
        assert item["mock_scheme"] == f"flowcast://pay/mock/{item['id']}"


def test_order_and_payment_404s_for_unknown_order() -> None:
    order_response = client.get("/orders/O404")
    status_response = client.get("/payments/status/O404")
    payment_response = client.post(
        "/payments/mock",
        json={"order_id": "O404", "method": "alipay", "status": "success"},
    )

    assert order_response.status_code == 404
    assert status_response.status_code == 404
    assert payment_response.status_code == 404
    assert order_response.json()["error"]["code"] == "ORDER_NOT_FOUND"
    assert status_response.json()["error"]["message"] == "Order not found"


def test_nfc_scenarios_include_display_and_action_fields() -> None:
    response = client.get("/nfc/scenarios")

    assert response.status_code == 200
    items = response.json()["items"]
    assert len(items) >= 3
    assert {item["id"] for item in items} >= {"merchant", "table", "miniapp"}
    for item in items:
        assert item["type"] in {"payment", "order", "miniapp"}
        assert item["tag"].startswith("NFC_")
        assert item["action"]
        assert item["scheme"].startswith("flowcast://nfc/")
        assert item["parsed_fields"]
        assert item["next_screen"].endswith("Screen")

    merchant = next(item for item in items if item["id"] == "merchant")
    assert merchant["parsed_fields"] == {
        "merchantId": "M10001",
        "amount": "20.00",
        "scene": "merchant_collect",
    }
    assert merchant["next_screen"] == "CashierScreen"

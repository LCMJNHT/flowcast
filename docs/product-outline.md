# FlowCast Demo Product Outline

## Positioning

FlowCast Demo is an internal prototype. It prioritizes the visible product workflow over full backend integration.

## First-Version Principles

- Views first: confirm the app structure, screens, and navigation before investing in infrastructure.
- Mock first: use local mock data and FastAPI mock responses for videos, news, orders, payment, and NFC scenarios.
- Closed-loop demo: every major user path should be clickable.
- Replaceable data sources: Android calls app/backend-facing contracts instead of hard-coding third-party providers.
- Production-shaped names: keep routes, models, and API names close to what a real product would use.

## Navigation

```text
Main shell
  Video
  News
  Cashier
  Profile
```

NFC is triggered from the Profile demo entry and by future Android NFC intents.

## Main Flows

### Video

```text
Open app
  -> Video tab
  -> Browse vertical feed
  -> View title, author, source, and engagement actions
```

### News

```text
Open News tab
  -> Browse AI news cards
  -> Tap a news item
  -> Read AI summary and key points
  -> Open original link
```

### Payment

```text
Open Cashier tab
  -> Review merchant and amount
  -> Select payment method
  -> Launch mock Scheme payment
  -> Return to payment result
```

### NFC

```text
Open NFC demo
  -> Select merchant collection, table ordering, or mini-program jump
  -> App parses the Scheme
  -> Navigate to the matching demo screen
```

## Deferred Logic

- Real third-party video API integration.
- News crawler and AI summarization jobs.
- Persistent order database.
- Real NFC tag writing and reading tests on device.
- Real payment-channel SDK integration.

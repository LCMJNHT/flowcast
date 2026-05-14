# FlowCast Demo

FlowCast Demo is an internal Android prototype for validating the first version of a short-video, AI-news, mock-payment, and NFC-triggered experience.

The first milestone is view-first:

- Build the Android Compose screens and navigation.
- Use mock data and mock API contracts.
- Keep interfaces close to the future production shape.
- Connect real data sources only after the product views are approved.

## Project Layout

```text
flowcast-demo/
  android/   Jetpack Compose Android prototype
  backend/   FastAPI mock service
  docs/      product design and API notes
```

## App Name

Chinese name: 闪阅 FlowCast

Repository name: `flowcast-demo`

## MVP Scope

- Short-video feed with third-party video-source placeholders.
- AI news list and summary detail pages.
- Cashier and Scheme-based mock payment flow.
- NFC demo entry for merchant collection, table ordering, and mini-program jumps.

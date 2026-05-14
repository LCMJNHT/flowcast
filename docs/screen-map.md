# FlowCast Demo Screen Map

```mermaid
flowchart TD
  A["MainShellScreen"] --> B["VideoFeedScreen"]
  A --> C["NewsListScreen"]
  A --> D["CashierScreen"]
  A --> E["ProfileScreen"]
  C --> F["NewsDetailScreen"]
  D --> G["MockPaymentScreen"]
  G --> H["PaymentResultScreen"]
  E --> I["NfcDemoScreen"]
  I --> D
  I --> J["TableOrderScreen"]
  I --> K["MiniProgramJumpScreen"]
```

## Android Screens

- `MainActivity`: Compose entry point and navigation host.
- `VideoFeedScreen`: visual-first video feed prototype.
- `NewsListScreen`: AI news list.
- `NewsDetailScreen`: AI summary and source link.
- `CashierScreen`: order and payment method selection.
- `MockPaymentScreen`: simulated third-party payment page.
- `PaymentResultScreen`: success, failed, or canceled result.
- `NfcDemoScreen`: manual trigger for NFC scenarios.
- `ProfileScreen`: demo information and utility entry points.

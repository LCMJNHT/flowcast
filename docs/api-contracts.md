# FlowCast Demo API Contracts

Base URL for local development:

```text
http://127.0.0.1:8000
```

## Videos

```http
GET /videos/feed
```

Returns a mock vertical video feed.

## News

```http
GET /news
GET /news/{id}
```

The list endpoint returns titles and summaries. The detail endpoint returns AI-style summaries, key points, and the original source URL.

## Orders

```http
POST /orders
GET /orders/{id}
```

Creates and reads demo orders.

## Payment

```http
POST /payments/mock
```

Updates a mock order payment status.

## NFC

```http
GET /nfc/scenarios
```

Returns demo NFC Schemes for merchant collection, table ordering, and mini-program jumping.

## Demo Schemes

```text
flowcast://pay/cashier?orderId=O10001&amount=88.00
flowcast://pay/mock/alipay?orderId=O10001
flowcast://pay/mock/wechat?orderId=O10001
flowcast://pay/mock/unionpay?orderId=O10001
flowcast://pay/result?orderId=O10001&status=success

flowcast://nfc/pay?merchantId=M10001&amount=20.00&scene=merchant_collect
flowcast://nfc/order?storeId=S10001&tableId=T08&scene=table_order
flowcast://nfc/miniprogram?platform=wechat&appId=wx123&path=pages/index/index
```

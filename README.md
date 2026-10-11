# Shipping Service

Python/FastAPI implementation of the legacy shipping calculation service.

## Setup

Requires Python 3.13 and PostgreSQL. Install dependencies with PDM:

```powershell
pdm install
```

Set `DATABASE_URL` and `CARRIER_URL` in `.env` when they differ from the defaults. The database schema is in `db/schema.sql` and the default carrier endpoint is `http://localhost:8081/api/v1/rates/evaluate`.

## Run

```powershell
pdm dev
```

The API is available at `http://localhost:8000`. The main endpoint is:

```text
POST /api/v2/checkout/calculate-shipping
```

It accepts the legacy JSON request contract and returns the calculated shipping fee, discounts, surcharges, and applied fee breakdown. Requests may supply `X-Trace-Id`; the same value is returned in the response header.

## Validate

```powershell
pdm check-all
```

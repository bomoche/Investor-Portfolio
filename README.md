# Enviro365 Investments — Withdrawal Notice System

Full-stack system allowing investors to view portfolios, submit withdrawal notices against validated business rules, and export filtered CSV statements.

Built for the eTalente / Enviro365 Junior Full-Stack Developer Assessment, June 2026.

---

## Repository Structure

```
Investor-Portfolio/
├── frontend/          ← React + Vite application
├── backend/           ← Spring Boot REST API
└── README.md
```

---

## Tech Stack

**Frontend:** React 19, Vite, React Router, Tailwind CSS v4, Axios

**Backend:** Spring Boot 3.5, Java 17, Spring Security + JWT, Spring Data JPA, H2 (in-memory), Lombok

---

## Getting Started

**Prerequisites:** Java 17, Node.js 20+

### Backend

```bash
cd backend
./mvnw spring-boot:run
```

Runs on `http://localhost:8080/api`. Seed data loads automatically on startup — no database setup needed.

H2 Console: `http://localhost:8080/api/h2-console`
JDBC URL `jdbc:h2:mem:enviro365db` · Username `sa` · Password blank

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Runs on `http://localhost:5173`. Proxies `/api` to the backend, so both must be running.

---

## Demo Credentials

Password for all accounts: `Password123!`

| Investor | Email | Age | Exercises |
|---|---|---|---|
| Thabo Mokoena | thabo.mokoena@enviro365.co.za | 71 | Retirement withdrawals permitted; has history for CSV |
| Naledi Dlamini | naledi.dlamini@enviro365.co.za | 33 | Retirement blocked by age rule |
| Sipho Khumalo | sipho.khumalo@enviro365.co.za | 47 | Single product, empty-state paths |

Also listed on the login screen behind a **Test accounts** panel with one-click fill. That panel is assessment scaffolding and would be removed in production.

---

## API Endpoints

| Method | Endpoint | Description | Auth |
|---|---|---|---|
| POST | `/api/auth/login` | Login, returns JWT | Public |
| GET | `/api/me/portfolio` | Portfolio with products and balances | Required |
| GET | `/api/me/products/{id}/eligibility` | Withdrawal eligibility for a product | Required |
| POST | `/api/me/withdrawals` | Submit withdrawal notice | Required |
| GET | `/api/me/withdrawals` | Withdrawal history | Required |
| GET | `/api/me/withdrawals/export` | Download CSV statement | Required |

Export accepts optional `productId`, `status`, `from` and `to` query parameters.

Endpoints use `/me` rather than `/investors/{id}` — the investor is resolved from the JWT, so one investor cannot access another's data by changing a URL.

### Error Responses

Every failure returns the same shape with a stable `errorCode`:

```json
{
  "timestamp": "2026-08-10T11:48:36.222",
  "status": 422,
  "error": "Unprocessable Entity",
  "errorCode": "EXCEEDS_WITHDRAWAL_LIMIT",
  "message": "Withdrawal of R115000.00 exceeds the maximum of R108000.00 (90% of the balance).",
  "path": "/api/me/withdrawals"
}
```

| Status | Meaning |
|---|---|
| 400 | Field validation failed — response includes
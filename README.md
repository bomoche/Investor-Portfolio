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
| 400 | Field validation failed — response includes `fieldErrors` |
| 401 | Missing, invalid or expired token; or rejected credentials |
| 404 | Resource absent or not owned by the caller |
| 422 | Business rule violation |

400 means the request couldn't be processed as sent (UI highlights form fields); 422 means it was understood and rejected on its merits (UI shows the business message).

---

## Business Rules

- Retirement withdrawals only permitted above age 65
- Withdrawal cannot exceed the available balance
- Withdrawal cannot exceed 90% of the available balance
- All monetary values use `BigDecimal` (precision 19, scale 2); the 90% ceiling rounds **down** to the cent

Rules are enforced in `WithdrawalValidator` before any state is mutated, so a rejected withdrawal leaves balances untouched. They run in the order above, so an under-age investor gets the age message rather than a percentage calculation that doesn't apply.

---

## Architecture

### Backend — Layered

`Controller → Service → Repository → Database`

- **Controller** — HTTP only, no business logic
- **Service** — business logic and rule validation
- **Repository** — Spring Data JPA interfaces
- **Entity** — JPA-mapped tables, never exposed to clients
- **DTO** — the API contract, separate from the schema

### Frontend — Feature-Sliced

`Pages → Hooks → API Layer → Backend`

- **Pages** — presentation
- **Hooks** — data fetching, loading and error state
- **API Layer** — one axios client with token and error interceptors

---

## Advanced Features Implemented

- Global exception handling via `@RestControllerAdvice`
- DTO layer — entities never leave the service layer
- Input validation with Jakarta Bean Validation
- Unit tests with JUnit 5 and Mockito
- UI validation with server-driven eligibility
- JWT authentication with Spring Security (beyond the brief)

---

## Testing

```bash
cd backend
./mvnw test
```

- `WithdrawalValidatorTest` — every business rule, including the age boundary, the exact-90% case, and rule precedence
- `WithdrawalServiceImplTest` — balance debit, before/after snapshots, that violations abort before mutation, and cross-account rejection

---

## Assumptions Made

- "Age > 65" is read literally: 65 does not qualify, 66 does. Pinned by a test so it can't drift silently.
- Withdrawals complete immediately. `WithdrawalStatus` includes `PENDING` and `REJECTED` so an approval workflow could be added without a schema change.
- Login identifier is email. The design labelled it "Investor ID"; the entity's unique identifier is email, so the field was relabelled rather than inventing an identifier the API can't resolve.
- The H2 database is in-memory and rebuilt on every restart, so the grader always gets a clean, seeded state.

---

## AI Tools Disclosure

As permitted by the assessment guidelines:

| Tool | Usage |
|---|---|
| **Stitch by Google** | UI/UX design — generated the Material 3 palette, type scale and layouts, ported into Tailwind v4 tokens and rebuilt as React components |
| **Claude** | Implementation assistance across backend and frontend |

System design and architecture were my own: the layered structure, storing rather than deriving balances, making `Investor` the authentication principal, adding JWT authentication beyond the brief, and the scoping decision to omit screens with no backend. All AI-assisted code was reviewed, run, debugged and verified by me.

---

## Contact

**Bongani Moche**
bonganimoche@wethinkcode.co.za
https://github.com/bomoche

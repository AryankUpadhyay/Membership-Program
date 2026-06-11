# FirstClub Membership Program

A production-grade Spring Boot backend for a tiered subscription membership system.

## Features

- **3 Plans**: Monthly (₹99), Quarterly (₹249), Yearly (₹799)
- **3 Tiers**: Silver / Gold / Platinum with configurable benefits
- **Benefit Engine**: Automatic discount + free delivery calculation at checkout
- **Auto Tier Promotion**: Order-driven tier upgrades/demotions (Strategy pattern)
- **Full Audit Trail**: Every membership change logged
- **Concurrent Safety**: Optimistic locking + pessimistic locking where needed
- **Scheduled Jobs**: Daily expiry check + tier re-evaluation + monthly stat resets

## Quick Start

### Option 1: Run locally with Maven
```bash
./mvnw spring-boot:run
```

### Option 2: Docker (recommended)
```bash
docker compose up --build
```

## Access Points

| URL | Description |
|-----|-------------|
| http://localhost:8080/api/v1/plans | Browse membership plans |
| http://localhost:8080/swagger-ui.html | Interactive API docs (Swagger UI) |
| http://localhost:8080/h2-console | H2 database console |

> H2 Console: JDBC URL = `jdbc:h2:mem:membershipdb`, User = `sa`, Password = *(empty)*

## API Reference

See [`API_REFERENCE.md`](./API_REFERENCE.md) for complete curl commands and an end-to-end demo flow.

## Architecture

```
REST Controllers
      │
  Services (Transactional)
      │
  Strategy / Chain-of-Responsibility (Tier Evaluation / Benefit Engine)
      │
  JPA Repositories
      │
  H2 In-Memory DB
```

### Design Patterns

| Pattern | Usage |
|---------|-------|
| **Strategy** | `TierRuleEvaluator` — pluggable rule evaluators per `RuleType` |
| **Chain of Responsibility** | `BenefitEngine` — stacks benefits in resolution order |
| **Optimistic Locking** | `@Version` on `UserMembership` prevents concurrent corruption |
| **Pessimistic Locking** | Subscribe + order stat update use `PESSIMISTIC_WRITE` to prevent races |
| **Repository Pattern** | Clean JPA data access layer |
| **DTO** | Request/response separation from domain model |

### Concurrency Safety

- **Subscribe**: Uses `REPEATABLE_READ` isolation + `PESSIMISTIC_WRITE` lock → prevents two concurrent calls from both creating an active membership for the same user
- **Upgrade/Downgrade**: `@Version` on `UserMembership` → `ObjectOptimisticLockingFailureException` on conflict → surfaced as 409 Conflict
- **Order Stats**: `PESSIMISTIC_WRITE` on counter update → prevents lost increments
- **Scheduler**: Runs in dedicated thread pool isolated from HTTP request threads

## Tech Stack

- Java 17 + Spring Boot 3.2
- Spring Data JPA + H2 (in-memory)
- Lombok + Bean Validation
- SpringDoc OpenAPI (Swagger UI)
- Docker (multi-stage build)

## Membership Tiers

| Tier | Orders/Month | GMV/Month | Discount | Free Delivery | Priority Support |
|------|:---:|:---:|:---:|:---:|:---:|
| Silver | ≥ 3 | - | 5% | ✓ | ✗ |
| Gold | ≥ 7 | ≥ ₹3,000 | 10% | ✓ | ✗ |
| Platinum | ≥ 15 | ≥ ₹7,500 | 15% | ✓ | ✓ |

> All thresholds and benefits are DB-configurable. No hardcoded business logic in services.
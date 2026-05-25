# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

```bash
# Build
mvn clean install

# Run (dev profile — port 8092, multi_empresa_test DB, show SQL)
mvn spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=dev"

# Run (prod — port 8093, multi_empresa DB)
java -jar target/multi-empresa-0.0.1-SNAPSHOT.jar

# Test
mvn test

# Single test class
mvn test -Dtest=MyServiceTest

# Skip tests
mvn clean install -DskipTests
```

## Architecture

### Package Layout

```
com.api.multiempresa/
├── configuration/       # SecurityConfig, OpenApiConfig, AppConfig
├── controller/          # Thin REST handlers (38 controllers)
├── dto/
│   ├── entity/          # Entity DTOs (40)
│   ├── request/         # Incoming payloads
│   ├── response/        # Outgoing payloads (ApiResponse<T> wrapper)
│   ├── filter/          # Search/filter params (paired with Specifications)
│   ├── mapper/          # MapStruct interfaces (26)
│   └── external/        # External API response shapes (SUNAT, APIPeru)
├── exception/           # GlobalExceptionHandler + custom exceptions
├── job/                 # Scheduled tasks (exchange rates, SUNAT sync)
├── repository/
│   └── spec/            # JPA Specification classes for dynamic WHERE clauses
├── security/            # JwtAuthFilter, JwtUtils, UserDetailsServiceImpl, TenantContext
├── service/
│   └── impl/            # Business logic
├── util/                # GoogleDriveOAuthUtils, PdfLogoResolver, validators
└── validation/          # Custom @Constraint validators
```

### Multi-Tenant Design

Project serves multiple companies from a single database. Every business entity has a `company_id` FK that scopes it to one company. Tables **without** `company_id` are shared master/reference tables (e.g. document types, UBIGEO, exchange rates, detraction codes) — they apply equally to all companies and are not filtered by tenant.

When creating a new table: decide first whether it's per-company or master data. If per-company, add `company_id BIGINT NOT NULL` with a FK to `company`.

### Request Lifecycle

`HTTP → JwtAuthFilter (sets TenantContext + SecurityContext) → Controller → Service → Repository`

TenantContext stores companyId in a ThreadLocal; services/specs use it to scope queries.

### Response Pattern

All endpoints return `ApiResponse<T>`. Use this wrapper for every response — success and error.

### Dynamic Queries

Filter DTOs + JPA `Specification` classes in `repository/spec/`. When adding a filterable endpoint:
1. Add fields to the filter DTO
2. Add predicates to the matching Specification class
3. Inject `JpaSpecificationExecutor` in the repository

### PDF Generation

JasperReports `.jrxml` templates live in `src/main/resources/jasper/`. Services in `service/impl/` compile and fill them at runtime. `PdfLogoResolver` fetches the company logo. ZXing generates QR codes embedded in the report.

### External Integrations

| System | Purpose |
|--------|---------|
| SUNAT | Send e-invoices/guides; receive ACKs; log results |
| APIPeru | Validate RUC/DNI against Peruvian registries |
| Google Drive | Store invoice PDFs, images, logos; organized by folder IDs in `application.yml` |

Google Drive uses OAuth tokens stored at `tokens/` (dev) or `/opt/multi-empresa/tokens` (prod). `client_secret.json` and `service-account.json` must be present.

### Scheduled Jobs

`job/` classes run via Spring `@Scheduled`. Current jobs: exchange rate import, SUNAT document status sync.

### Security

- Public: `/api/auth/**`, `/api/document-types/**`, Swagger UI
- All other routes: Bearer JWT required
- CORS origins configured in `SecurityConfig` (add new origins there, not in YAML)

### Flyway Migrations

Files: `src/main/resources/db/migration/V###__description.sql`
Current latest: V067. New migrations: V068, V069, … (strictly sequential).
See MEMORY.md for MySQL 8.0 DDL constraints and repair procedure.

## Key Conventions

- `ApiResponse<T>` — always wrap responses
- MapStruct mappers — never map manually in service code
- Filter DTO + Specification — for any list endpoint with search params
- `TenantContext.getCompanyId()` — get current company in service layer
- `JwtUtils.extractUsernameFromContext()` returns `ruc:username`; split on `:` for each part

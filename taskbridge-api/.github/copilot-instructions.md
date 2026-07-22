# Copilot Instructions - Multi-Tenant B2B SaaS (Java 21 / Spring Boot 3)

## 1) Layered Architecture

- Use strict layers: `api (controllers)` -> `application (services)` -> `domain` -> `infrastructure (repositories/external clients)`.
- Keep dependencies one-way; lower layers must not depend on upper layers.
- Controllers only orchestrate HTTP concerns (request mapping, validation, response codes).
- Business rules belong in services/domain models, never in controllers or repositories.
- Repositories are persistence adapters only; no business decisions in repository implementations.
- Use constructor injection exclusively; avoid field injection.
- Keep transactional boundaries in services using `@Transactional`.
- Use package-by-feature (`projects`, `notifications`, etc.) with consistent internal subpackages (`api`, `service`, `domain`, `repository`, `dto`, `mapper`).

## 2) DTO Requirements

- Never expose JPA entities in API contracts.
- Define separate DTOs for input and output (`CreateProjectRequest`, `ProjectResponse`).
- Use immutable DTOs where possible (`record` preferred in Java 21).
- Include only required fields; avoid over-fetching and over-posting.
- Map DTOs explicitly (manual mapper or dedicated mapper class); avoid reflection-heavy implicit mapping.
- Include tenant-safe identifiers only; do not expose cross-tenant internal references.
- Version externally visible DTOs when introducing breaking changes.

## 3) Validation Standards

- Apply Bean Validation (`jakarta.validation`) on request DTOs.
- Validate at API boundary using `@Valid` / `@Validated`.
- Use standard constraints: `@NotNull`, `@NotBlank`, `@Size`, `@Email`, `@Pattern`, `@Positive`, etc.
- Add custom validators for domain-specific rules (e.g., tenant-scoped uniqueness, naming conventions).
- Return consistent error format with field, message, rejected value (when safe), and correlation ID.
- Fail fast on invalid input; do not allow partial writes on validation failure.

## 4) Security Rules

- All endpoints are authenticated by default; explicitly whitelist public endpoints.
- Use Spring Security with stateless JWT authentication.
- Validate JWT signature, expiry, issuer, audience, and required claims.
- Enforce authorization with least privilege (`@PreAuthorize` and role/permission checks).
- Never trust tenant identifiers from request body/path alone; derive tenant context from trusted token claims.
- Deny by default: missing/invalid auth or tenant context must return `401`/`403`.
- Store secrets in secure configuration sources; never hardcode credentials or keys.
- Sanitize all inputs and encode outputs where applicable to prevent injection attacks.

## 5) Tenant Isolation Requirements

- Every tenant-owned table must include `tenant_id` and proper indexing (typically composite indexes with business keys).
- Every read/write query must be tenant-scoped; no unbounded cross-tenant queries.
- Enforce tenant filters at multiple levels:
  - security context establishes current tenant,
  - service layer asserts tenant ownership,
  - repository layer applies tenant predicates.
- Treat absence of tenant context as a security violation.
- Prevent IDOR: always verify resource belongs to current tenant before access/update/delete.
- Background jobs, async flows, and events must carry and validate tenant context.
- Add tests specifically for cross-tenant access denial.

## 6) Logging Standards

- Use structured logging (JSON preferred in non-local environments).
- Include `traceId`, `spanId`, `tenantId`, `userId`, and request correlation IDs when available.
- Log security-relevant events (auth failures, access denials, privilege changes).
- Never log secrets or sensitive data (passwords, tokens, PII unless explicitly masked).
- Use log levels consistently:
  - `ERROR`: failed operations requiring action,
  - `WARN`: suspicious/recoverable conditions,
  - `INFO`: key business milestones,
  - `DEBUG`: diagnostic details (non-production by default).
- Keep log messages actionable and concise.

## 7) Testing Standards

- Use JUnit 5 for unit/integration tests; Mockito for mocking collaborators.
- Minimum expectations per feature:
  - unit tests for business logic and edge cases,
  - repository tests for tenant-scoped queries,
  - API tests for authn/authz and validation behavior.
- Unit tests must be deterministic, isolated, and fast (no external network calls).
- Prefer constructor-based dependency injection in test fixtures for clarity.
- Validate negative paths explicitly (`401`, `403`, `404`, validation errors, tenant mismatch).
- Use test data builders/factories for readability.
- Name tests using behavior format: `should_<expected>_when_<condition>`.
- Keep coverage focused on critical logic (authorization, tenancy, state transitions) rather than chasing raw percentages.

## 8) Documentation Standards

- Every feature must include:
  - API contract updates (OpenAPI/Swagger),
  - security and permission requirements,
  - tenant behavior notes,
  - migration notes for schema changes.
- Public endpoints must document request/response examples and error cases.
- Document non-obvious architectural decisions in ADRs.
- Keep README and module docs current with setup, run, test, and environment variables.
- For each pull request, include:
  - problem statement,
  - design approach,
  - security/tenant impact,
  - testing evidence.
- Treat documentation as part of the definition of done.

## Implementation Defaults (Apply Unless Explicitly Overridden)

- Java: `21`
- Framework: `Spring Boot 3.x`
- Database: `PostgreSQL`
- Persistence: `Spring Data JPA`
- Security: `Spring Security + JWT`
- Testing: `JUnit 5 + Mockito`


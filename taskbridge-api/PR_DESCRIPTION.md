# PR Description

## Summary
- Refactors the `projects` slice into a stricter layered architecture (API -> application service -> domain -> repository) with transport-neutral command/query models.
- Adds audit and notification domain models, repositories, services, and REST APIs with tenant-scoped access patterns.
- Introduces security and reliability hardening: tenant-context validation, method authorization hooks, conflict/error mapping, and structured logging improvements.
- Adds focused JUnit 5 + Mockito coverage for core project, audit, and notification scenarios.

## Motivation
- Reduce cross-layer coupling by removing REST DTO dependencies from application services.
- Strengthen multi-tenant safety by constraining repository interfaces and enforcing tenant/organization predicates consistently.
- Improve production readiness through explicit validation, immutable audit modeling, and better operational/error semantics.
- Provide a foundation for future compliance controls (audit integrity, privacy-safe logging, retention governance).

## Integration details
- Project integration:
  - `ProjectService` now uses command/query/result models and API mappers handle DTO conversion.
  - Repository surface is tenant-safe and avoids broad unscoped access methods.
- Audit integration:
  - Immutable `AuditEntry` entity and `AuditService` support create + scoped history retrieval.
  - Added project-scoped history support and optional date-range filtering in audit query flow.
  - Endpoints added: `POST /audit`, `GET /audit/{projectId}`.
- Notification integration:
  - `NotificationService` supports create, recipient-scoped query, and mark-read behavior.
  - Added team dispatch helper service for equal fan-out to all team members.
  - Endpoints added: `GET /notifications/{userId}`, `PATCH /notifications/{id}/read`.
- Security integration:
  - Added `AuthorizationService` hooks used via `@PreAuthorize` on new controllers.
  - Added unauthorized/forbidden/invalid-parameter exception mapping in global handler.
- Data integration:
  - Flyway migration includes `audit_entries` and `notifications` tables with tenant-first indexes and constraints.

## AI Disclosure
- Portions of this change set were AI-assisted for scaffolding, refactoring, and test generation.
- Human judgment was applied for:
  - multi-tenant security boundaries,
  - authorization hook design,
  - error semantics and HTTP mappings,
  - audit/notification architectural trade-offs,
  - compliance/privacy impact analysis artifacts.
- The final structure and risk-sensitive decisions were manually reviewed and adjusted.

## Testing Summary
- Added/updated unit tests (JUnit 5 + Mockito) for:
  - project service/domain validations and tenant-scoped behavior,
  - audit service creation, event/project/date filtering, immutability checks,
  - notification creation/read behavior and recipient/organization isolation,
  - equal notification dispatch to all team members,
  - controller-level mapping behavior and exception handler mappings.
- Static code error checks for modified files reported no issues.
- Runtime `mvn test` execution was attempted multiple times but skipped by environment controls.

## Risk Assessment
- **Overall risk:** Medium (broad refactor + new feature slices + API additions).
- **Primary risks:**
  - behavior drift from service-contract refactor,
  - terminology mismatch (`tenantId` vs `organizationId`) across slices,
  - method-security hooks currently conservative and may need stricter role policies,
  - migration sequencing dependency for environments with existing data.
- **Mitigations:**
  - concentrated unit and scenario coverage,
  - tenant-scoped repository methods only,
  - explicit exception mappings,
  - migration-first rollout guidance,
  - architecture and impact docs (`REVIEW.md`, `IMPACT_ANALYSIS.md`, `ARCHITECTURE.md`).

## Self Review Checklist
- [x] Layer boundaries are respected (controllers map DTOs; services use commands/results).
- [x] Tenant/organization scoping is applied in repository lookups and service operations.
- [x] Input validation exists at API and domain/application boundaries.
- [x] Error responses map to appropriate HTTP statuses (`400/401/403/404/409/500`).
- [x] Structured logs include action/outcome context without leaking obvious secrets.
- [x] New DB tables/indexes/constraints are captured via Flyway migration.
- [x] Tests cover critical behaviors and negative paths.
- [ ] Full runtime test suite execution is pending due environment skip.

## Peer Review Comments
1. **Security (AI-missed risk):** The audit IP capture path should not trust raw `X-Forwarded-For` headers without a trusted proxy chain policy; AI-generated code often misses this and stores spoofable IPs. Please add canonical client-IP extraction and tests for spoofed headers.
2. **Architecture consistency:** We now mix `tenantId` and `organizationId` terminology across slices. Please standardize naming (or add explicit mapping docs) to avoid subtle authorization and query bugs in future integrations.
3. **Release confidence:** Since runtime `mvn test` was skipped in this environment, add one CI-gated test run result (or attach pipeline evidence) before merge, especially for migration + controller/security touch points.


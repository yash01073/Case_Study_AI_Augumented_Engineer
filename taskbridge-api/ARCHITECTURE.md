# Architecture
- `ProjectService` owns project lifecycle operations (create, update, status transitions, list by team, delete) with transaction boundaries and domain validation.
- `NotificationService` owns notification creation, recipient-scoped queries, and read-state transitions.
- Integration contract: project workflows emit notification intents via application commands (including team fan-out), not direct controller-to-repository coupling.
- Layered architecture: Controller (HTTP + validation) -> Service (business rules + orchestration) -> Domain (entity invariants) -> Repository (tenant-scoped persistence).
- Data flow: JWT is validated, tenant/user context is resolved, controller maps DTOs to commands, service executes, mapper returns response DTOs.
- Audit and notification writes are append/update operations constrained by domain methods and repository contracts.
- Tenant isolation is enforced by trusted context (`TenantContext`) and organization/tenant predicates in every repository lookup.
- Cross-tenant access resolves to not-found semantics on scoped queries to avoid tenant enumeration.
- Structured logging captures action, outcome, tenant/organization, actor, and resource identifiers for traceability.
- Trade-off: strict layering increases mapping boilerplate but reduces coupling and improves testability.
- Trade-off: command/query models improve transport neutrality but add extra types to maintain.
- Trade-off: tenant-safe repository surfaces reduce accidental data leakage but constrain generic data-access convenience.
- Trade-off: Trade-off: Current design favors consistency using synchronous audit creation.This increases reliability but may introduce additional latency during project updates.


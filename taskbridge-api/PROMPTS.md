# PROMPTS.md

## Purpose
This document records the major prompts used during the TaskBridge case study and how Copilot outputs were validated and corrected.

## Prompt Log

### 1) Copilot instruction baseline
- **Prompt text:** "Create a `.github/copilot-instructions.md` for a multi-tenant B2B SaaS app with Java 21, Spring Boot 3, PostgreSQL, JPA, Security, JWT, Mockito, JUnit 5."
- **Copilot feature:** Chat + Edit
- **Prompting technique:** Constraint-first specification (explicit section list and stack)
- **Result:** Created policy-style coding guide with architecture, DTO, validation, security, tenancy, logging, testing, and documentation rules.
- **Human validation:** Reviewed for alignment with intended standards and consistency of MUST/SHOULD language.
- **Human corrections:** Clarified enforceable guidance and kept rules concise for maintainability.

### 2) Project model + service generation
- **Prompt text:** "Generate a Project model and a Project service with create, update status, get by team, and delete functions. Use a database."
- **Copilot feature:** Chat + Inline Suggestions + Edit
- **Prompting technique:** Feature-slice generation (entity + repository + service + controller + migration)
- **Result:** Implemented full project slice with status transitions, tenant-scoped repository methods, APIs, and tests.
- **Human validation:** Reviewed service behavior, tenant checks, and transition semantics.
- **Human corrections:** Added stronger invariants and later refactored service boundaries.

### 3) Project README generation
- **Prompt text:** "Generate README.md for a Spring Boot based TaskBridge Notification & Audit Service project."
- **Copilot feature:** Chat + Edit
- **Prompting technique:** Structured doc generation (fixed section headings)
- **Result:** Added comprehensive README with architecture, stack, setup, build/test instructions, and folder layout.
- **Human validation:** Checked that commands and file references match workspace reality.
- **Human corrections:** Removed overclaims and kept environment assumptions explicit.

### 4) Specification generation
- **Prompt text:** "Create SPEC.md for a Notification & Audit Service with functional/non-functional requirements, data models, API contracts, authorization, validation, integration points."
- **Copilot feature:** Chat + Edit + Workspace Context
- **Prompting technique:** Requirements-first architecture draft
- **Result:** Produced a detailed specification with API/data model contracts and open decisions.
- **Human validation:** Cross-checked against code implementation and security expectations.
- **Human corrections:** Identified and documented missing controls (tamper evidence, internal auth rigor, retention/legal hold detail).

### 5) Project entity review
- **Prompt text:** "Review this Project entity for production readiness (validation, security, data integrity, JPA best practices, multi-tenancy)."
- **Copilot feature:** Explain + Chat
- **Prompting technique:** Risk-based review by category
- **Result:** Produced severity-ranked findings and remediation recommendations.
- **Human validation:** Verified findings against actual entity/repository/schema behavior.
- **Human corrections:** Prioritized tenant isolation and optimistic locking as top risks.

### 6) Project service review
- **Prompt text:** "Review this ProjectService for architecture, security, error handling, validation, logging, tenant isolation, transaction management."
- **Copilot feature:** Explain + Workspace Context + Chat
- **Prompting technique:** End-to-end request path review (controller -> service -> repository -> security)
- **Result:** Produced issue list with severity/risk/remediation.
- **Human validation:** Confirmed reported gaps against code and handler mappings.
- **Human corrections:** Converted findings into concrete hardening patches.

### 7) Code patch hardening request
- **Prompt text:** "do a code patch"
- **Copilot feature:** Edit + Inline Suggestions
- **Prompting technique:** Iterative hardening patch
- **Result:** Added stricter repository surface, service validation, JWT fail-fast behavior, and expanded exception mappings/tests.
- **Human validation:** Checked for compile errors and cross-file consistency.
- **Human corrections:** Refined boundaries and error semantics in follow-up patches.

### 8) Review artifact generation
- **Prompt text:** "Generate REVIEW.md from identified findings... include Finding, Severity, Impact, Detection Method, Remediation."
- **Copilot feature:** Chat + Edit
- **Prompting technique:** Audit-style structured reporting
- **Result:** Produced review document with categorized findings and a Copilot-specific human-judgment section.
- **Human validation:** Ensured findings map to real files and risks.
- **Human corrections:** Kept language actionable, not purely descriptive.

### 9) Layered architecture refactor request
- **Prompt text:** "Refactor this Project Service into a production-grade Spring Boot layered architecture..."
- **Copilot feature:** Edit + Workspace Context + Inline Suggestions
- **Prompting technique:** Contract-first refactor (API DTOs -> application commands/results)
- **Result:** Refactored `ProjectService` to transport-neutral contracts and moved mapping to API layer.
- **Human validation:** Verified affected controllers/tests/repositories compile and behavior stays tenant-safe.
- **Human corrections:** Added dedicated unauthorized context exception and improved conflict/error mappings.

### 10) Audit and notification entity generation
- **Prompt text:** "Generate JPA entities for AuditEntry and Notification with indexes and validation."
- **Copilot feature:** Chat + Inline Suggestions + Edit
- **Prompting technique:** Domain-first model generation with schema support
- **Result:** Added entities, repositories, migration, and domain tests.
- **Human validation:** Checked immutability intent, constraints, and tenant-first indexing.
- **Human corrections:** Extended model for project-scoped audit lookup where required.

### 11) Audit/notification service generation
- **Prompt text:** "Generate Spring Boot services for AuditService and NotificationService..."
- **Copilot feature:** Edit + Inline Suggestions
- **Prompting technique:** Application-layer command/query/result pattern
- **Result:** Added services, mappers, commands/queries/results, and Mockito tests.
- **Human validation:** Verified tenant predicates and not-found semantics for cross-org access.
- **Human corrections:** Added recipient ownership check for mark-read behavior.

### 12) REST controller generation
- **Prompt text:** "Generate REST controllers for POST /audit, GET /audit/{projectId}, GET /notifications/{userId}, PATCH /notifications/{id}/read."
- **Copilot feature:** Edit + Workspace Context
- **Prompting technique:** Endpoint contract-driven implementation
- **Result:** Added controllers, DTOs, API mappers, authorization hooks, and exception mappings.
- **Human validation:** Checked endpoint-to-service command mapping and context-sourced tenant/user identity.
- **Human corrections:** Added access-denied/type-mismatch handler mappings and ownership checks.

### 13) Impact analysis request
- **Prompt text:** "Analyze impact of MILESTONE_REOPENED and actor IP capture; include DB/DTO/API/security/compliance/migration impact."
- **Copilot feature:** Chat + Workspace Context
- **Prompting technique:** Change-impact matrix
- **Result:** Produced detailed impact analysis and rollout strategy.
- **Human validation:** Compared analysis to current schema/services/controllers.
- **Human corrections:** Highlighted GDPR/privacy/retention/logging gaps in separate impact artifact.

### 14) Mockito scenario tests request
- **Prompt text:** "Generate JUnit5 and Mockito tests covering dispatch, audit creation, immutability, date filtering, event filtering, unauthorized org access."
- **Copilot feature:** Chat + Inline Suggestions + Edit
- **Prompting technique:** Scenario-based testing with negative paths
- **Result:** Added scenario test suite and supporting team dispatch harness.
- **Human validation:** Confirmed each requested scenario is represented by explicit assertions.
- **Human corrections:** Extended audit query model to support date-range path under test.

### 15) Architecture/PR/tooling docs requests
- **Prompt text:** Requests for `ARCHITECTURE.md`, `PR_DESCRIPTION.md`, `TOOL_STRATEGY.md`, and additions/updates.
- **Copilot feature:** Edit + Workspace Context
- **Prompting technique:** Documentation-as-code with constrained templates
- **Result:** Generated reviewer-facing artifacts with risks, integration details, AI disclosure, and tool strategy.
- **Human validation:** Ensured documentation reflects actual implemented files and known limitations.
- **Human corrections:** Added peer-review comments and explicit limitations section.

## Post-Generation Corrections

1. **Migration versioning correction:** Avoided editing applied Flyway versions in place; documented forward-only migration requirement.
2. **Authorization tightening gap:** Marked permissive auth hooks as temporary and flagged need for authority-based policies.
3. **Tenant terminology drift:** Documented mismatch between `tenantId` and `organizationId` and flagged standardization need.
4. **Error mapping correction:** Added explicit handling for `IllegalArgumentException`, `AccessDeniedException`, and optimistic locking conflicts.
5. **JWT failure behavior correction:** Updated filter behavior to fail fast with `401` on invalid tokens.
6. **Notification ownership correction:** Enforced recipient check during mark-read to prevent intra-tenant user-level access leaks.
7. **Audit query capability correction:** Added project/date/event query support to align implementation with requested scenarios.
8. **Compliance/privacy correction:** Added `IMPACT_ANALYSIS.md` to capture GDPR/privacy/retention/logging controls missing from initial generation.

## Notes
- Copilot accelerated scaffolding, refactors, and test generation, but high-risk design decisions still required human review.
- Security, tenancy, migration strategy, and compliance controls were validated manually before finalizing artifacts.


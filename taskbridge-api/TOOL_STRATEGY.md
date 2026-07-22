# TOOL_STRATEGY.md

## Purpose
This document records practical usage of GitHub Copilot features during implementation of the TaskBridge multi-tenant backend.

## Feature Usage Log

### 1) Chat — Architecture decomposition
- **Purpose:** Break down a broad refactor request into entity/repository/service/controller/DTO/validation/exception tasks.
- **Outcome:** Produced a staged implementation path that reduced cross-layer regressions and kept tenant isolation visible at each step.
- **Why chosen:** Chat is best for rapid design iteration and converting ambiguous requirements into actionable work items.

### 2) Chat — Security and compliance gap review
- **Purpose:** Identify missing security, privacy, GDPR, retention, and logging controls in specification and code.
- **Outcome:** Generated `IMPACT_ANALYSIS.md`, `REVIEW.md`, and concrete remediation themes for authorization, retention, and audit controls.
- **Why chosen:** Chat excels at synthesizing cross-cutting concerns that span code, architecture, and governance artifacts.

### 3) Inline Suggestions — Boilerplate acceleration
- **Purpose:** Speed up repetitive implementation of commands, query models, record DTOs, and JavaDoc blocks.
- **Outcome:** Faster creation of consistent classes (`Create*Command`, `*View`, mapper methods, repository signatures).
- **Why chosen:** Inline suggestions reduce typing overhead while preserving local context in the active file.

### 4) Edit — Safe large-file refactors
- **Purpose:** Apply coordinated edits across service contracts, controller mappings, and exception handling without manual drift.
- **Outcome:** `ProjectService` was converted to transport-neutral command/query/result signatures, with API mappers handling DTO conversion.
- **Why chosen:** Edit mode is effective for structural refactors where multiple symbols must be changed consistently.

### 5) Explain — Legacy/AI-generated code validation
- **Purpose:** Understand inherited generated code paths (tenant context, JWT filter behavior, repository scoping, transaction semantics).
- **Outcome:** Surfaced subtle risks (over-permissive auth hooks, migration checksum risk, unscoped repository exposure patterns).
- **Why chosen:** Explain helps verify intent vs behavior before patching potentially sensitive paths.

### 6) Workspace Context — Cross-file impact tracing
- **Purpose:** Resolve change impact across schema, domain model, services, controllers, and tests before applying updates.
- **Outcome:** Correctly propagated changes like audit project/date filtering and notification recipient-safe read behavior across all affected files.
- **Why chosen:** Workspace context is essential when one requirement modifies multiple layers and test suites.

### 7) Chat + Inline Suggestions — Test generation strategy
- **Purpose:** Build scenario-driven JUnit 5 + Mockito coverage for dispatch parity, audit creation/immutability, filters, and unauthorized access.
- **Outcome:** Added targeted scenario tests and a minimal dispatch harness (`TeamNotificationDispatchService`) to make behavior testable.
- **Why chosen:** Chat provided test strategy; inline suggestions accelerated repetitive Mockito setup and assertions.

### 8) Edit + Workspace Context — PR artifact production
- **Purpose:** Produce coherent engineering artifacts (`ARCHITECTURE.md`, `PR_DESCRIPTION.md`, `IMPACT_ANALYSIS.md`) aligned with real code.
- **Outcome:** Generated reviewer-ready documents with integration details, AI disclosure, risk assessment, and peer review comments.
- **Why chosen:** Edit ensured consistency while workspace context anchored claims to implemented files.

## Notes
- Copilot outputs were treated as accelerators, not final authority; security, tenancy, migration, and compliance-sensitive decisions required human review.
- For high-risk paths, prompts were constrained to explicit acceptance criteria (tenant scoping, status codes, validation, and traceability).

## Scenario responses

### Scenario 1: Architecture assessment (layer boundaries, service roles, integration contracts)
- **Best feature:** Chat
- **Why:** Chat is strongest for decomposing broad architectural prompts into explicit layers, responsibilities, and contracts before touching code.

### Scenario 2: Cross-file impact assessment (schema + entity + service + API + tests)
- **Best feature:** Workspace Context
- **Why:** Workspace Context links related files and symbols so multi-layer changes can be applied consistently and missing dependencies are easier to spot.

### Scenario 3: Fast implementation of repetitive patterns (DTOs, commands, mappers, JavaDoc)
- **Best feature:** Inline Suggestions
- **Why:** Inline Suggestions accelerates high-volume, pattern-based coding while preserving developer control over naming and behavior.

### Scenario 4: Large refactor with coordinated symbol changes
- **Best feature:** Edit
- **Why:** Edit is ideal for safely applying structured updates across multiple methods/files (for example, contract changes from DTO-driven to command/query-driven services).

### Scenario 5: Understanding inherited or AI-generated code behavior before modifying it
- **Best feature:** Explain
- **Why:** Explain clarifies intent versus actual runtime behavior (auth flow, transaction boundaries, tenant scoping) and reduces risky blind edits.

### Scenario 6: Security/compliance review (GDPR, privacy, retention, logging exposure)
- **Best feature:** Chat + Workspace Context
- **Why:** Chat synthesizes control gaps and mitigations, while Workspace Context anchors each finding to concrete code/config/schema locations for actionable remediation.

### Scenario 7: Test strategy for scenario-based verification
- **Best feature:** Chat + Inline Suggestions
- **Why:** Chat helps define test cases and negative paths; Inline Suggestions speeds up Mockito scaffolding, captors, and assertion patterns.

### Scenario 8: PR and review artifact generation
- **Best feature:** Edit + Workspace Context
- **Why:** Edit keeps documentation coherent and consistently formatted, while Workspace Context ensures claims align with actual implementation changes.

## Limitations Encountered

### 1) Migration versioning safety (common AI miss)
- **Prompt:** "Patch the audit schema to support project-scoped history and new fields quickly."
- **Problem:** The model tendency is to modify an existing Flyway migration file in place, which can create checksum drift in shared environments.
- **Detection:** Human review flagged that changing a previously applied migration breaks Flyway consistency across environments.
- **Fix:** Introduced forward-only migration guidance and moved schema evolution to new versioned migrations.
- **Improved approach:** Prompt with explicit constraint: "Never edit applied Flyway migrations; create a new `V{n+1}__...sql` migration only."

### 2) Over-permissive authorization hooks
- **Prompt:** "Add authorization hooks for new audit and notification endpoints."
- **Problem:** Generated hooks defaulted to `authenticated == true` for broad operations, which is functional but not least-privilege.
- **Detection:** Security review identified that read/write audit paths should be authority-gated, not just authentication-gated.
- **Fix:** Kept hooks as extension points, documented risk, and added review comments requiring stricter authority policies.
- **Improved approach:** Prompt with explicit policy targets: "Enforce `AUDIT_READ`/`AUDIT_WRITE` and user-scoped notification access; include deny-path tests."

### 3) Incomplete privacy handling for actor IP capture
- **Prompt:** "Add actor IP capture impact and implementation details for audit records."
- **Problem:** Initial output focused on schema/model wiring and under-specified trusted proxy extraction, masking policy, and GDPR handling.
- **Detection:** Compliance/privacy review surfaced missing lawful-basis, minimization, and logging-exposure controls.
- **Fix:** Added `IMPACT_ANALYSIS.md` with GDPR/privacy/retention/logging risk controls and implementation checklists.
- **Improved approach:** Prompt with governance requirements up front: "Include lawful basis, DSAR implications, trusted-header policy, masking defaults, and retention controls."


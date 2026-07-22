# IMPACT_ANALYSIS.md

## Scope

This document reviews the existing impact analysis for introducing:

1. `MILESTONE_REOPENED` audit event
2. actor IP address capture in audit records

and identifies missing concerns in:

- GDPR
- privacy
- retention
- logging exposure risks

---

## What Was Already Covered

The prior analysis correctly covered:

- enum and schema updates for `MILESTONE_REOPENED`
- adding `ip_address` to `audit_entries`
- service/DTO/controller propagation impacts
- basic spoofing risk (`X-Forwarded-For`) and migration sequencing

---

## Missing Concerns Identified

## 1) GDPR Concerns (Missing)

| Missing Concern | Risk | Affected Components | Recommended Mitigation |
|---|---|---|---|
| No explicit lawful basis for storing actor IP addresses (personal data in many jurisdictions) | High | `audit_entries.ip_address`, `AuditController`, privacy docs | Document lawful basis (e.g., security/fraud prevention), Data Protection Impact Assessment (DPIA) decision, and processing purpose limitation |
| No data-subject rights handling strategy for immutable audit records (access, rectification objections, erasure constraints) | High | `AuditService`, export endpoints (future), compliance procedures | Define DSAR policy specifically for immutable audit logs; support access/export while documenting erasure exceptions for legal/security obligations |
| No data minimization policy for IP retention granularity | High | DB schema and response payloads | Decide and enforce one of: full IP, truncated/masked IP, or hashed IP with salt; default to minimal necessary representation |
| No cross-border transfer and processor/subprocessor posture documented for audit data | Medium | infrastructure/runtime, backups, observability stack | Record data residency policy, backup locations, and subprocessors in compliance artifacts |
| No GDPR-ready Records of Processing Activities (RoPA) update path | Medium | governance docs (`SPEC.md`, compliance docs) | Add RoPA entry for audit IP processing with purpose, categories, retention, recipients, and safeguards |

### GDPR-specific implementation checklist

- [ ] Add lawful basis and purpose statement for IP capture to `SPEC.md`
- [ ] Add DSAR handling guidance for immutable audit records
- [ ] Add data minimization rule (mask/hash/full) for IP values
- [ ] Add RoPA/compliance mapping for audit IP processing
- [ ] Add evidence artifacts expected for audit/compliance reviews

---

## 2) Privacy Concerns (Missing)

| Missing Concern | Risk | Affected Components | Recommended Mitigation |
|---|---|---|---|
| IP captured without clear trust chain/normalization policy | High | `AuditController`, `JwtAuthFilter`, proxy config | Define trusted proxy list, canonical client IP extraction order, and reject untrusted forwarding headers |
| No explicit field-level access control for IP visibility in API responses | High | `AuditEntryResponse`, audit read endpoints | Restrict IP visibility by role/permission (`AUDIT_IP_READ`) or return masked value by default |
| Potential over-collection in `previous_state` and `new_state` snapshots | High | `AuditEntry`, audit producers | Define redaction policy for personal/sensitive fields before persistence |
| No “privacy by default” response policy | Medium | audit APIs and exports | Exclude `ipAddress` by default; provide privileged opt-in projection for authorized investigators |
| No breach impact model update for newly stored personal metadata | Medium | incident response process | Update incident response playbooks to treat audit IP as personal data exposure |

### Privacy-specific implementation checklist

- [ ] Add canonical client IP extraction utility
- [ ] Add masking/redaction utility for IP and sensitive state snapshots
- [ ] Add role-gated IP exposure policy for audit API responses
- [ ] Add security tests for header spoofing and untrusted proxy scenarios
- [ ] Update incident response data classification matrix

---

## 3) Retention Concerns (Missing)

| Missing Concern | Risk | Affected Components | Recommended Mitigation |
|---|---|---|---|
| No retention schedule split between audit payload and IP metadata | High | `audit_entries`, archival jobs | Define separate retention windows (e.g., audit event vs. raw IP), where legal obligations allow |
| No legal hold override behavior defined for deletion/archival jobs | High | archival/purge workflow (future), compliance controls | Implement legal-hold flags and hard precedence over purge tasks |
| No backup retention alignment with primary-table retention | Medium | DB backups, disaster recovery | Align backup lifecycle with retention policy and document residual retention after deletion |
| No retention enforcement mechanism specified (batch job, partitioning, archiving) | Medium | DB operations, maintenance jobs | Implement explicit retention mechanism and monitoring (partition drop/archive with audit trail) |
| No retention evidence/reporting requirement | Medium | governance and audits | Generate periodic retention compliance reports with deletion/archival evidence |

### Retention-specific implementation checklist

- [ ] Define retention policy for IP and audit states separately
- [ ] Define legal hold model and precedence
- [ ] Implement purge/archive mechanism with immutable control logs
- [ ] Align backup retention with policy
- [ ] Add retention compliance reporting output

---

## 4) Logging Exposure Risks (Missing)

| Missing Concern | Risk | Affected Components | Recommended Mitigation |
|---|---|---|---|
| IP may be logged accidentally in app logs and exception traces | High | `AuditServiceImpl`, `GlobalExceptionHandler`, log appenders | Add logging policy: never log full IP in INFO/WARN; mask/hash in structured logs |
| `previous_state` / `new_state` may contain PII and could be logged on errors | High | service/controller exception paths | Ensure snapshot payloads are never emitted verbatim in logs; redact before logging |
| No centralized sensitive-field logging filter | High | logging configuration (`application.yml`) and appenders | Add log sanitization filter/interceptor for known sensitive keys (`ip`, `token`, `email`, etc.) |
| Missing role-based access control for log platform containing personal metadata | Medium | log storage / SIEM | Enforce least-privilege access and audit read access to logs |
| No explicit observability retention policy alignment with application retention | Medium | log pipeline | Align SIEM/log retention with data classification and audit retention policy |

### Logging-risk implementation checklist

- [ ] Add logging redaction rules for IP and state payloads
- [ ] Add tests verifying no full IP or raw state appears in logs
- [ ] Restrict log platform access by role and monitor log-access events
- [ ] Align log retention policy with privacy and legal requirements
- [ ] Update runbooks for sensitive-data logging incidents

---

## Priority Matrix

| Priority | Items |
|---|---|
| Critical | Trusted IP extraction policy, IP visibility control, DSAR/legal basis definition |
| High | Data minimization for IP, state snapshot redaction, legal hold retention design |
| Medium | Backup retention alignment, logging platform RBAC hardening, compliance evidence automation |

---

## Recommended Affected Files to Update Next

### Code / schema
- `src/main/java/com/taskbridge/audit/domain/AuditEntry.java`
- `src/main/java/com/taskbridge/audit/service/command/CreateAuditEntryCommand.java`
- `src/main/java/com/taskbridge/audit/service/result/AuditEntryView.java`
- `src/main/java/com/taskbridge/audit/api/AuditController.java`
- `src/main/java/com/taskbridge/audit/dto/AuditEntryResponse.java`
- `src/main/java/com/taskbridge/common/api/GlobalExceptionHandler.java`
- `src/main/resources/db/migration/V3__extend_audit_entries_for_milestone_reopened_and_ip_capture.sql` (new)
- `src/main/resources/application.yml`

### Tests
- `src/test/java/com/taskbridge/audit/api/AuditControllerTest.java`
- `src/test/java/com/taskbridge/audit/service/AuditServiceImplTest.java`
- `src/test/java/com/taskbridge/common/api/GlobalExceptionHandlerTest.java`

### Documentation and governance
- `SPEC.md`
- `REVIEW.md`
- privacy/compliance artifacts (RoPA, retention policy, incident playbook)

---

## Final Recommendation

Before implementing actor IP capture in production, treat it as a privacy/security feature (not just a schema field):

1. define lawful basis and minimization policy,
2. implement trusted extraction and masking,
3. enforce role-based exposure in API/logs,
4. establish retention + legal hold behavior,
5. document compliance evidence requirements.

This closes the key GDPR, privacy, retention, and logging exposure gaps missing from the original impact analysis.

## Human Validation

Copilot correctly identified schema and API impacts.

Additional concerns identified manually:

- IP addresses constitute personal data
- Retention policies must be defined
- Log masking requirements are needed
- GDPR data minimization principles must be considered

These concerns were added to the final analysis.
`


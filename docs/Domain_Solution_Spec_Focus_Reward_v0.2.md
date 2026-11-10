# Domain / Solution Specification
## Focus Reward — MVP

**Document version:** 0.2  
**Status:** Implementation-ready domain design draft  
**Date:** 2026-10-28  
**Source BRD:** `BRD_Focus_Reward_MVP_v0.3.md`  
**Purpose:** Convert the approved MVP business model into precise domain concepts, invariants, state machines, algorithms, transactional operations, persistence constraints, API behavior, and implementation boundaries suitable for Java/Spring Boot development.

---

# 1. Scope of This Specification

This document defines the solution-level behavior required before implementation of:

- Spring entities;
- repositories;
- services;
- migrations;
- REST API;
- authentication/authorization;
- focus-session lifecycle;
- XP/level calculation;
- reward calculation;
- goals/mastery/prerequisites;
- maintenance routines;
- template revisions/forks;
- export/import;
- account deletion;
- core integration tests.

This document does **not** define:

- final visual design;
- final frontend framework/component structure;
- production infrastructure vendor;
- AI integration;
- social systems;
- advanced RPG systems;
- final product branding.

The BRD remains the source of truth for **why** the product exists and what the MVP must validate. This document is the source of truth for **how the MVP domain behaves**.

---

# 2. Architectural Style

## 2.1 Application shape

MVP should be implemented as a **modular monolith**.

Recommended backend stack:

- Java 21+
- Spring Boot
- Spring Web
- Spring Security
- OAuth2 Client / OpenID Connect for Google login
- Spring Data JPA / Hibernate
- PostgreSQL
- Flyway
- Jakarta Validation
- Testcontainers PostgreSQL for integration testing

No microservices, message broker, distributed cache, event bus, or background worker is required for MVP.

## 2.2 Why modular monolith

The main complexity is domain logic and data integrity, not horizontal scaling.

A single transactional boundary is valuable for operations such as:

- finishing/correcting focus;
- recalculating reward;
- passing mastery;
- publishing template revisions;
- forking template graphs;
- deleting an account.

The application should still be split into clear feature modules/packages.

Suggested modules:

```text
auth
user
path
skill
goal
mastery
quest
focus
reward
maintenance
template
export
feedback
common
```

## 2.3 Layering

Preferred dependency direction:

```text
HTTP / Controller
      ↓
Application Service
      ↓
Domain Model / Domain Services
      ↓
Repository Interfaces
      ↓
JPA / PostgreSQL
```

Persistence entities may be the domain entities for MVP if disciplined boundaries are maintained, but API DTOs must remain separate from entities.

API input must never bind directly to JPA entities.

---

# 3. Global Technical Conventions

## 3.1 IDs

Use server-generated UUIDs.

MVP requirement:

```text
UUID
```

Exact UUID version is not product-significant. UUIDv4 is sufficient.

Imported/template-generated content never supplies authoritative database IDs.

## 3.2 Time

All timestamps are stored as UTC instants:

```text
TIMESTAMPTZ
Instant in Java
```

User calendar behavior uses:

```text
IANA timezone
e.g. Europe/Berlin
```

Never persist ambiguous local timestamps for events.

## 3.3 Calendar dates

Use `LocalDate` only when the business concept itself is a local calendar date:

- target date;
- reward accounting date;
- maintenance completion date.

## 3.4 Money

Reward credit is bookkeeping, not money held by the service.

Store monetary values as integer euro cents:

```text
100 = €1.00
500 = €5.00
```

Do not use floating-point types.

## 3.5 XP

XP is an integer count of confirmed full focus minutes.

```text
1 credited minute = 1 XP
```

Use `BIGINT`/`long` for lifetime totals.

## 3.6 Duration

Persist actual confirmed duration in seconds.

Derived XP:

```text
credited_minutes = floor(confirmed_seconds / 60)
```

Persist `credited_minutes` on `FocusSession` as the canonical awarded minute count after confirmation/correction.

Seconds remain available for accurate history/UI.

## 3.7 Optimistic locking

Mutable user/admin aggregates that can reasonably be edited from multiple tabs should have a version field:

```java
@Version
long version;
```

At minimum:

- SkillGoal
- Skill
- Quest
- MaintenanceRoutine
- TemplateRevision draft

Focus finalization additionally relies on explicit state transitions/idempotency.

## 3.8 Soft deletion / archival

Historical progression must not disappear when a user cleans their tree.

Prefer `archived_at` or domain status for:

- Path
- Skill
- Quest
- MaintenanceRoutine

Hard deletion is allowed only for unused objects with no historical references, or as part of full account deletion.

---

# 4. High-Level Domain Model

```text
UserAccount
 ├── PersonalPath
 │    └── SkillNode
 │         ├── SkillGoal
 │         ├── MasteryCriterion
 │         ├── MasteryAttempt
 │         ├── Quest
 │         ├── SkillPrerequisite
 │         ├── FocusSession
 │         └── MaintenanceRoutine
 │              └── MaintenanceCompletion
 │
 ├── RewardTransaction
 ├── RewardDailyAccrual
 └── Feedback

Template
 └── TemplateRevision (validated JSON document)
        ↓ fork
PersonalPath + relational user-domain records
```

Important design choice:

> **User progression is relational and operational. Template revisions are immutable versioned content documents stored as validated JSONB.**

This makes:

- admin editing simpler;
- revision history explicit;
- future structured AI import natural;
- forks deterministic;
- user progression independent from templates.

---

# 5. Core Invariants

These invariants must hold regardless of API/client behavior.

## INV-001 — Ownership

Every personal object ultimately belongs to exactly one user.

A user cannot access another user's:

- paths;
- skills;
- goals;
- mastery;
- quests;
- focus;
- maintenance;
- rewards;
- exports.

## INV-002 — Skill node type

A `CATEGORY` skill:

- may contain child skills;
- cannot receive direct focus;
- cannot own an active training goal;
- cannot receive direct XP.

A `TRAINABLE` skill:

- must be a leaf;
- may receive focus;
- may have goals/mastery/quests/maintenance.

## INV-003 — One active goal

A trainable skill has at most one `ACTIVE` goal.

A skill may simultaneously have any number of `PLANNED` goals that already exist but are not currently being pursued.

Therefore the valid cardinality is:

```text
0..N PLANNED
0..1 ACTIVE
0..N closed goals
```

Activating a `PLANNED` goal while another `ACTIVE` goal exists requires the current active goal to become:

- ACHIEVED;
- REPLACED;
- ABANDONED.

A `PLANNED` goal does not satisfy the active-goal requirement for normal training focus.

## INV-004 — Training requires a goal

A trainable skill may be focused only if it currently has an `ACTIVE` goal.

Exception:

A skill in maintenance mode may be focused for maintenance without an active training goal.

## INV-005 — Mastery is optional

`mastery_enabled = false` means:

- no mastery attempts;
- no mastery-level prerequisite can target that skill.

## INV-006 — Mastery is sequential

If mastery is enabled:

```text
0 → 1 → 2 → 3 → 4
```

A PASS can only target:

```text
current_mastery_level + 1
```

## INV-007 — No XP from mastery/quests/goals

XP derives only from completed confirmed focus sessions.

## INV-008 — Parent aggregation is derived

Parent/category XP and time are display aggregates of descendants.

They never create additional character XP.

## INV-009 — One active stopwatch

A user can have at most one `ACTIVE` focus session.

`REVIEW_REQUIRED` sessions do not block starting a new session.

## INV-010 — Focus sanity threshold

A session with raw elapsed time greater than 10 hours cannot receive automatic credit.

It must be reviewed.

## INV-011 — Maximum credited single session

Any single focus session can credit at most:

```text
600 minutes
```

## INV-012 — Reward eligibility is snapshotted

Historical reward outcomes do not change merely because a user later toggles a skill's reward eligibility.

## INV-013 — Reward day is stable

Each focus session receives one immutable `reward_local_date` using the user's timezone snapshot at session start.

Changing user timezone later does not move historical reward between days.

## INV-014 — Reward daily cap

Earned focus reward on a local day cannot exceed:

```text
€5.00 = 500 cents
```

## INV-015 — Reward spending cannot create negative balance

A spend is rejected if:

```text
current_balance < requested_spend
```

## INV-016 — Prerequisites are same-path

MVP prerequisite edges may only connect skills within the same personal path.

## INV-017 — Prerequisite graph is acyclic

No prerequisite cycle is allowed.

## INV-018 — Hierarchy graph is acyclic

No skill can become its own ancestor.

## INV-019 — Template fork independence

After fork, user-domain records are independent.

Publishing/editing a template revision never mutates an existing fork.

## INV-020 — Published template revision content is immutable

Admin editing of published content creates a new draft revision.

The previous published JSON payload is not overwritten.

---

# 6. UserAccount

## 6.1 Purpose

Represents the authenticated account and user-level settings.

## 6.2 Fields

| Field | Type | Rules |
|---|---|---|
| id | UUID | PK |
| google_subject | String | unique, required |
| email | String | required |
| email_verified | boolean | from Google |
| display_name | String | nullable |
| avatar_url | String | nullable |
| timezone | String | valid IANA zone, required before focus |
| role | enum | USER / ADMIN |
| created_at | Instant | required |
| updated_at | Instant | required |
| last_login_at | Instant | required |
| deleted_at | Instant | nullable |

## 6.3 Google authentication rule

The Google OIDC `sub` claim is the stable external identity key.

Do not use email as the external identity primary key.

Do not persist Google access/refresh tokens unless a future Google API feature requires them.

## 6.4 Timezone onboarding

On first login:

1. account may temporarily hold a server default;
2. frontend reads browser IANA timezone;
3. frontend sends it to backend;
4. focus start is blocked until a valid timezone is confirmed.

---

# 7. PersonalPath

## 7.1 Purpose

Top-level user-owned progression tree.

Examples:

```text
Backend Engineering
German
Strength / Calisthenics
Piano
```

## 7.2 Fields

| Field | Type |
|---|---|
| id | UUID |
| user_id | UUID FK |
| name | String(120) |
| description | Text nullable |
| source_template_id | UUID nullable |
| source_template_revision | int nullable |
| archived_at | Instant nullable |
| created_at | Instant |
| updated_at | Instant |
| version | long |

## 7.3 Rules

- path names need not be unique;
- archived path remains visible in history but not default active views;
- all contained skills must share path ownership;
- prerequisite edges cannot cross path boundaries in MVP.

---

# 8. SkillNode

## 8.1 Enum: SkillNodeType

```text
CATEGORY
TRAINABLE
```

## 8.2 Purpose

Represents both organizational branches and concrete trainable leaves.

## 8.3 Fields

| Field | Type | Notes |
|---|---|---|
| id | UUID | PK |
| path_id | UUID FK | required |
| parent_id | UUID FK self | nullable |
| node_type | enum | CATEGORY/TRAINABLE |
| name | String(120) | required |
| description | Text | nullable |
| sort_order | int | sibling ordering |
| reward_eligible | boolean | relevant to TRAINABLE |
| mastery_enabled | boolean | relevant to TRAINABLE |
| current_mastery_level | smallint | 0–4; 0 if disabled |
| source_template_node_key | String nullable | provenance only |
| archived_at | Instant nullable | |
| created_at | Instant | |
| updated_at | Instant | |
| version | long | |

## 8.4 Structural rules

### CATEGORY

May have children.

Cannot:
- be selected in focus;
- own current goal;
- own maintenance routine;
- receive direct XP.

### TRAINABLE

Must have zero children.

May:
- have goals;
- be focused;
- own quests;
- have optional mastery;
- have maintenance routine.

## 8.5 Node-type conversion

Before any progression history exists:

```text
CATEGORY ↔ TRAINABLE
```

may be allowed subject to structural validity.

After any of these exist:

- focus history;
- goal history;
- mastery attempts;
- maintenance history;

node type becomes immutable.

This prevents historical direct XP from later becoming category XP.

---

# 9. SkillGoal

## 9.1 Enum: GoalStatus

```text
PLANNED
ACTIVE
ACHIEVED
REPLACED
ABANDONED
```

## 9.2 Fields

| Field | Type |
|---|---|
| id | UUID |
| skill_id | UUID FK |
| title | String(160) |
| success_criterion | Text |
| baseline_value | Decimal nullable |
| target_value | Decimal nullable |
| unit | String(40) nullable |
| target_date | LocalDate nullable |
| status | GoalStatus |
| outcome_note | Text nullable |
| started_at | Instant |
| closed_at | Instant nullable |
| created_at | Instant |
| updated_at | Instant |
| version | long |

## 9.3 Rules

- skill must be TRAINABLE;
- any number of PLANNED goals may exist per skill;
- at most one ACTIVE goal may exist per skill;
- `success_criterion` is mandatory for both PLANNED and ACTIVE goals;
- numeric fields are optional;
- backend does not attempt semantic AI validation of whether criterion is "good";
- UI provides examples/guidance;
- ACHIEVED is user-confirmed, never inferred from XP;
- closed goal is never silently overwritten.

## 9.4 Goal transitions

```text
PLANNED ─────────────► ACTIVE
   │                    │
   │                    ├────────► ACHIEVED
   │                    ├────────► REPLACED
   │                    └────────► ABANDONED
   │
   └──────────────────► ABANDONED
```

Rules:

- `PLANNED → ACTIVE` is allowed only if the skill currently has no other ACTIVE goal.
- `PLANNED → ABANDONED` removes a future goal without ever activating it.
- `ACTIVE → ACHIEVED | REPLACED | ABANDONED` closes the current goal.
- closed states are terminal.
- after closing the active goal, any PLANNED goal may be activated.

---

# 10. SkillPrerequisite

## 10.1 Enum: PrerequisiteType

```text
GOAL_ACHIEVED
MASTERY_AT_LEAST
```

## 10.2 Fields

| Field | Type |
|---|---|
| id | UUID |
| skill_id | UUID FK | dependent skill |
| prerequisite_skill_id | UUID FK | required skill |
| type | enum |
| required_mastery_level | smallint nullable |
| created_at | Instant |

## 10.3 Validation

General:

- same user;
- same path;
- IDs differ;
- no duplicate identical condition;
- resulting prerequisite graph remains acyclic.

For `GOAL_ACHIEVED`:

```text
required_mastery_level = NULL
```

Satisfied when prerequisite skill has at least one historical goal with:

```text
status = ACHIEVED
```

For `MASTERY_AT_LEAST`:

- prerequisite skill must have mastery enabled;
- required level ∈ 1..4.

Satisfied when:

```text
prerequisite_skill.current_mastery_level >= required_mastery_level
```

## 10.4 Unlock rule

A skill with zero prerequisites is available structurally.

A skill with prerequisites is unlocked only if **all** prerequisite conditions are satisfied.

Unlock is derived, not persisted as source-of-truth state.

---

# 11. Derived Skill Presentation State

Do not persist one ambiguous "skill status" field.

Derive presentation state in this order:

```text
if archived:
    ARCHIVED
else if prerequisites unmet:
    LOCKED
else if active maintenance routine exists:
    MAINTENANCE
else if active goal exists:
    TRAINING
else if at least one achieved goal exists:
    ACHIEVED
else:
    AVAILABLE
```

Possible UI states:

```text
LOCKED
AVAILABLE
TRAINING
ACHIEVED
MAINTENANCE
ARCHIVED
```

`Mastery` remains a separate axis.

Example:

```text
state = TRAINING
mastery = INDEPENDENT
```

is valid.

---

# 12. Mastery

## 12.1 Default mastery levels

```text
0 UNKNOWN
1 GUIDED
2 INDEPENDENT
3 DEFENSIBLE
4 EVIDENCE
```

Names are fixed in MVP.

## 12.2 MasteryCriterion

Fields:

| Field | Type |
|---|---|
| id | UUID |
| skill_id | UUID FK |
| level | smallint 1..4 |
| criterion | Text |
| created_at | Instant |
| updated_at | Instant |
| version | long |

Constraint:

```text
UNIQUE(skill_id, level)
```

A mastery-enabled skill should define a criterion before that level can be attempted.

## 12.3 MasteryAttempt

Enum:

```text
PASS
FAIL
```

Fields:

| Field | Type |
|---|---|
| id | UUID |
| skill_id | UUID FK |
| target_level | smallint |
| result | enum |
| criterion_snapshot | Text |
| evidence_note | Text nullable |
| evidence_url | String nullable |
| attempted_at | Instant |

## 12.4 PASS transaction

Within one transaction:

1. lock skill;
2. verify `mastery_enabled`;
3. verify target = current + 1;
4. load criterion;
5. create attempt with criterion snapshot;
6. if FAIL:
   - no mastery mutation;
7. if PASS:
   - increment `current_mastery_level`;
8. commit.

Dependent unlock state becomes different automatically because it is derived.

## 12.5 Criterion edits

Editing a criterion does not revoke historical mastery.

Past attempts preserve:

```text
criterion_snapshot
```

so historical meaning is not lost.

---

# 13. Quest

## 13.1 Enum: QuestStatus

```text
OPEN
COMPLETED
ARCHIVED
```

## 13.2 Enum: ContentSource

```text
USER
TEMPLATE
```

Future:

```text
AI
```

## 13.3 Fields

| Field | Type |
|---|---|
| id | UUID |
| skill_id | UUID FK |
| goal_id | UUID FK nullable |
| title | String(180) |
| description | Text nullable |
| status | enum |
| source | enum |
| completed_at | Instant nullable |
| archived_at | Instant nullable |
| created_at | Instant |
| updated_at | Instant |
| version | long |

## 13.4 Rules

- skill must be TRAINABLE;
- goal, if present, must belong to same skill;
- completing quest does not create XP;
- completing quest does not create mastery;
- archived quest cannot be selected for a new focus session.

---

# 14. FocusSession

## 14.1 Enum: FocusSessionStatus

```text
ACTIVE
REVIEW_REQUIRED
COMPLETED
DISCARDED
```

## 14.2 Fields

| Field | Type | Notes |
|---|---|---|
| id | UUID | |
| user_id | UUID FK | denormalized ownership for fast checks/index |
| skill_id | UUID FK | TRAINABLE |
| quest_id | UUID FK nullable | |
| status | enum | |
| started_at | Instant | source of stopwatch |
| stopped_at | Instant nullable | time user finished/reviewed |
| raw_elapsed_seconds | int nullable | recorded when leaving ACTIVE |
| confirmed_seconds | int nullable | 0..36000 |
| credited_minutes | int | floor(confirmed_seconds/60) |
| reward_eligible_snapshot | boolean | copied from skill at start |
| reward_local_date | LocalDate | derived at start |
| timezone_snapshot | String | IANA zone at start |
| note | Text nullable | |
| corrected | boolean | true if duration manually changed |
| correction_note | Text nullable | optional |
| created_at | Instant | |
| updated_at | Instant | |
| version | long | |

## 14.3 Reward date

At focus start:

```text
timezone_snapshot = user.timezone
reward_local_date =
    local date of started_at in timezone_snapshot
```

This value never changes.

This intentionally favors deterministic MVP behavior over complex cross-midnight splitting.

## 14.4 Focus start preconditions

To start:

- user timezone exists;
- skill belongs to user;
- skill is TRAINABLE;
- skill not archived;
- prerequisite conditions satisfied;
- either:
  - skill has ACTIVE goal, or
  - skill has ACTIVE maintenance routine;
- no other ACTIVE session remains after stale normalization.

## 14.5 Stale normalization

Before starting a new session or reading current session:

```text
if ACTIVE and now - started_at > 10h:
    status = REVIEW_REQUIRED
    raw_elapsed_seconds = elapsed
    stopped_at = now
```

No XP/reward is awarded.

Then a new ACTIVE session may be started.

## 14.6 Normal finish

When finish is requested:

1. lock session;
2. if COMPLETED:
   - return existing completed result (idempotent);
3. if DISCARDED:
   - conflict;
4. calculate raw elapsed.

If raw elapsed:

```text
> 10h
```

then:

```text
ACTIVE → REVIEW_REQUIRED
```

No credit.

Otherwise:

- default `confirmed_seconds = raw_elapsed_seconds`;
- client may provide corrected duration;
- validate 0..36000 seconds;
- set credited minutes;
- transition to COMPLETED;
- recalculate affected reward day;
- return progression summary.

## 14.7 Manual correction of completed session

A completed session may be corrected.

Rules:

- new confirmed duration 0..36000 seconds;
- update `credited_minutes`;
- mark `corrected=true`;
- XP automatically changes because XP derives from focus sessions;
- reward day is recalculated;
- session remains COMPLETED.

This is self-tracking, not anti-cheat.

## 14.8 REVIEW_REQUIRED resolution

User must choose:

### Correct

Provide actual duration 0..36000 seconds.

Then:

```text
REVIEW_REQUIRED → COMPLETED
```

### Discard

```text
REVIEW_REQUIRED → DISCARDED
```

Discard awards no XP/reward.

## 14.9 Cancel active session

User can cancel:

```text
ACTIVE → DISCARDED
```

No credit.

## 14.10 State machine

```text
                  elapsed > 10h
              ┌────────────────────┐
              ▼                    │
ACTIVE ───────────────► REVIEW_REQUIRED
  │                          │
  │ finish <= 10h            │ correct
  ▼                          ▼
COMPLETED ◄──────────────────┘
  ▲
  │ edit duration
  └─────────────── COMPLETED

ACTIVE ───── cancel ─────► DISCARDED
REVIEW_REQUIRED ─ discard ► DISCARDED
```

Terminal business states:

```text
COMPLETED
DISCARDED
```

A COMPLETED session is editable only in confirmed duration/note, not resurrected into ACTIVE.

---

# 15. XP Source of Truth

## 15.1 No XP ledger in MVP

Do not store mutable `skill.xp` or `user.character_xp` as authoritative balances.

Source of truth:

```text
FocusSession.status = COMPLETED
FocusSession.credited_minutes
```

## 15.2 Skill XP

For a trainable skill:

```text
skill_xp =
SUM(credited_minutes)
WHERE status = COMPLETED
AND skill_id = X
```

## 15.3 Character XP

```text
character_xp =
SUM(credited_minutes)
WHERE status = COMPLETED
AND user_id = X
```

Each session appears once, therefore no parent double-counting is possible.

## 15.4 Category XP

For display:

```text
category_xp =
SUM(credited_minutes)
for completed sessions on descendant TRAINABLE skills
```

This is an aggregate only.

## 15.5 Caching

Do not introduce XP cache tables initially.

If queries become expensive later, materialized/cached totals may be added while sessions remain the source of truth.

---

# 16. Character Level Formula

## 16.1 Constants

```text
MAX_LEVEL = 100
MAX_XP = 600000
K = 0.05
```

## 16.2 Threshold function

For level `L ∈ [1, 100]`:

```text
threshold(L) =
round(
    600000 *
    (exp(0.05 * (L - 1)) - 1)
    /
    (exp(0.05 * 99) - 1)
)
```

Guarantees:

```text
threshold(1) = 0
threshold(100) = 600000
```

## 16.3 Character level

```text
level(xp) =
max L where threshold(L) <= xp
```

If:

```text
xp >= 600000
```

then:

```text
level = 100
```

Lifetime XP remains visible beyond the cap.

## 16.4 Reference thresholds

| Level | Total XP | Approx. focused hours |
|---:|---:|---:|
| 1 | 0 | 0 |
| 2 | 219 | 3.65 |
| 5 | 948 | 15.8 |
| 10 | 2,433 | 40.6 |
| 20 | 6,787 | 113 |
| 30 | 13,967 | 233 |
| 40 | 25,805 | 430 |
| 50 | 45,322 | 755 |
| 60 | 77,500 | 1,292 |
| 70 | 130,553 | 2,176 |
| 80 | 218,022 | 3,634 |
| 90 | 362,234 | 6,037 |
| 95 | 466,334 | 7,772 |
| 99 | 570,529 | 9,509 |
| 100 | 600,000 | 10,000 |

## 16.5 Implementation rule

Create one pure domain service:

```text
CharacterLevelCalculator
```

It owns the constants/formula and is unit-tested with fixed reference thresholds.

Do not distribute formula logic across controller/frontend/database.

Frontend may reproduce it for animation only, but backend is authoritative.

---

# 17. Reward Domain

Unlike XP, reward has spending and therefore needs an auditable ledger.

## 17.1 RewardDailyAccrual

Purpose:

Persist the calculated daily earning state and simplify locking/recalculation.

Fields:

| Field | Type |
|---|---|
| id | UUID |
| user_id | UUID FK |
| local_date | LocalDate |
| eligible_minutes | int |
| earned_cents | int 0..500 |
| updated_at | Instant |
| version | long |

Constraint:

```text
UNIQUE(user_id, local_date)
```

## 17.2 RewardTransaction

Enum:

```text
FOCUS_EARNING_ADJUSTMENT
SPEND
```

Fields:

| Field | Type |
|---|---|
| id | UUID |
| user_id | UUID FK |
| type | enum |
| amount_cents | int signed |
| local_date | LocalDate nullable |
| description | String(240) nullable |
| created_at | Instant |

Rules:

For `FOCUS_EARNING_ADJUSTMENT`:
- amount may be positive or negative;
- local_date required.

For `SPEND`:
- amount must be negative;
- description required.

## 17.3 Daily reward formula

For user `U`, date `D`:

```text
eligible_minutes(D) =
SUM(credited_minutes)
for completed focus sessions
where:
    user_id = U
    reward_local_date = D
    reward_eligible_snapshot = true
```

Target earned cents:

```text
earned_cents(D) =
min(
    floor(eligible_minutes(D) / 20) * 100,
    500
)
```

Examples:

```text
0..19 min   = €0
20..39      = €1
40..59      = €2
60..79      = €3
80..99      = €4
100+        = €5
```

## 17.4 Recalculation algorithm

Whenever a completed session is:

- finalized;
- corrected from one duration to another;

run in the same transaction:

1. identify its immutable `reward_local_date`;
2. lock/create `RewardDailyAccrual(user,date)`;
3. recompute eligible minutes from focus sessions;
4. calculate target earned cents;
5. compare to previous `earned_cents`;
6. `delta = target - previous`;
7. update daily accrual;
8. if delta != 0:
   - insert `RewardTransaction`
   - type = FOCUS_EARNING_ADJUSTMENT
   - amount = delta.

This means a correction can create:

```text
+100 cents
```

or:

```text
-100 cents
```

without rewriting historical transactions.

## 17.5 Reward balance

```text
balance_cents =
SUM(reward_transaction.amount_cents)
```

No mutable balance field is authoritative.

## 17.6 Spend operation

Transaction:

1. lock user row or use equivalent serialization mechanism;
2. calculate current balance;
3. validate requested amount > 0;
4. ensure balance >= amount;
5. insert `SPEND` transaction with `-amount`;
6. commit.

No overdraft.

## 17.7 Reward eligibility snapshot

When focus starts:

```text
reward_eligible_snapshot =
skill.reward_eligible
```

Changing the skill later does not retroactively affect completed sessions.

---

# 18. MaintenanceRoutine

## 18.1 Routine status

```text
ACTIVE
PAUSED
ARCHIVED
```

At most one ACTIVE routine per skill.

## 18.2 Cadence type

```text
SELECTED_WEEKDAYS
TIMES_PER_WEEK
EVERY_N_DAYS
```

## 18.3 Fields

| Field | Type |
|---|---|
| id | UUID |
| skill_id | UUID FK |
| minimum_action | String(240) |
| cadence_type | enum |
| selected_weekdays | bit mask / integer nullable |
| times_per_week | smallint nullable |
| every_n_days | smallint nullable |
| start_date | LocalDate |
| cue_text | String(240) nullable |
| status | enum |
| created_at | Instant |
| updated_at | Instant |
| version | long |

## 18.4 Validation

### SELECTED_WEEKDAYS

- at least one weekday selected;
- other cadence parameters null.

### TIMES_PER_WEEK

```text
1 <= times_per_week <= 7
```

Other cadence params null.

### EVERY_N_DAYS

```text
1 <= every_n_days <= 365
```

Other cadence params null.

## 18.5 MaintenanceCompletion

Fields:

| Field | Type |
|---|---|
| id | UUID |
| routine_id | UUID FK |
| completed_on | LocalDate |
| completed_at | Instant |
| focus_session_id | UUID nullable |
| note | String(240) nullable |

Constraint for MVP:

```text
at most one counted completion per routine per local date
```

Repeated activity can still exist as focus sessions, but only one routine completion counts per day.

## 18.6 Cadence semantics

### SELECTED_WEEKDAYS

For each selected weekday:

- completion on that local date satisfies that occurrence;
- missing it is recorded historically as a miss;
- missed occurrences do not create accumulating "debt";
- next selected day is the next opportunity.

Current UI state:
- DUE_TODAY if today is selected and no completion today;
- COMPLETE_TODAY if completed today;
- NOT_DUE otherwise.

### TIMES_PER_WEEK

Week definition:

```text
Monday 00:00 → Sunday 23:59
in user timezone
```

State includes:

```text
completed X / target Y
```

No carry-over debt into next week.

### EVERY_N_DAYS

If never completed:

```text
first due date = start_date
```

After completion on date D:

```text
next_due_date = D + N days
```

No mastery loss if overdue.

## 18.7 Maintenance + focus

A routine can be completed:

### Directly

No XP/reward.

Useful for actions not meaningfully timed.

### Through focus

Link a completed focus session to a maintenance completion.

Normal focus rules apply.

---

# 19. Template System

## 19.1 Template

Fields:

| Field | Type |
|---|---|
| id | UUID |
| slug | String unique |
| display_name | String |
| description | Text |
| visibility | enum |
| current_published_revision_id | UUID nullable |
| created_at | Instant |
| updated_at | Instant |

Visibility:

```text
PUBLIC
HIDDEN
```

MVP may expose PUBLIC only.

## 19.2 TemplateRevision

Enum:

```text
DRAFT
PUBLISHED
ARCHIVED
```

Fields:

| Field | Type |
|---|---|
| id | UUID |
| template_id | UUID FK |
| revision_number | int |
| status | enum |
| schema_version | String |
| document | JSONB |
| created_by | UUID FK nullable |
| created_at | Instant |
| updated_at | Instant |
| published_at | Instant nullable |
| version | long |

Constraint:

```text
UNIQUE(template_id, revision_number)
```

## 19.3 Published edit behavior

When admin clicks Edit on a published revision:

1. backend creates new revision:
   - `revision_number = latest + 1`;
   - status = DRAFT;
   - document = deep copy of latest published JSON;
2. admin edits draft;
3. validation runs;
4. publish:
   - status → PUBLISHED;
   - `published_at = now`;
   - template.current_published_revision_id = new revision.

Old published document never changes.

## 19.4 Template document v1

Canonical shape:

```json
{
  "schemaVersion": "template-v1",
  "path": {
    "name": "Backend Engineering",
    "description": "..."
  },
  "skills": [
    {
      "key": "spring",
      "parentKey": null,
      "type": "TRAINABLE",
      "name": "Spring Boot",
      "description": "...",
      "rewardEligible": true,
      "masteryEnabled": true,
      "suggestedGoal": {
        "title": "...",
        "successCriterion": "...",
        "targetValue": null,
        "unit": null
      },
      "masteryCriteria": [
        {
          "level": 1,
          "criterion": "..."
        }
      ],
      "starterQuests": [
        {
          "key": "spring-q1",
          "title": "...",
          "description": "..."
        }
      ],
      "maintenanceSuggestion": null
    }
  ],
  "prerequisites": [
    {
      "skillKey": "spring-security",
      "requiresSkillKey": "spring",
      "type": "MASTERY_AT_LEAST",
      "requiredMasteryLevel": 2
    }
  ]
}
```

## 19.5 Template validation

Before publication:

- schema version supported;
- skill keys unique;
- all references resolve;
- hierarchy acyclic;
- prerequisite graph acyclic;
- TRAINABLE node has no children;
- CATEGORY node has no suggested active goal/mastery/maintenance;
- mastery criteria levels unique/valid;
- mastery prerequisite only points to mastery-enabled source skill;
- no duplicate prerequisite condition;
- text/size limits respected.

## 19.6 Fork algorithm

Within one transaction:

1. load published revision;
2. validate/read document;
3. create `PersonalPath`;
4. create all `SkillNode`s;
5. map:
   ```text
   template key → generated UUID
   ```
6. create suggested goals as ACTIVE personal goals by default **only after user confirmation in onboarding**;
7. create mastery criteria;
8. create starter quests;
9. create prerequisite edges using mapped UUIDs;
10. optionally create maintenance suggestions only as suggestions, not active routines;
11. save provenance:
    - source template id;
    - source revision number;
    - source node key;
12. commit.

Important:

The user must be able to edit/remove suggested content before or after starting.

---

# 20. Structured Admin Import

## 20.1 Purpose

Allow administrator to use an external AI/tool manually without integrating AI into product runtime.

## 20.2 Pipeline

```text
JSON upload/paste
      ↓
parse
      ↓
schema validation
      ↓
domain validation
      ↓
TemplateRevision DRAFT
      ↓
admin preview/edit
      ↓
publish
```

## 20.3 Trust boundary

Imported fields may define content only.

They cannot define:

- DB IDs;
- user ownership;
- XP;
- current mastery state;
- focus history;
- reward balance;
- timestamps controlled by server;
- publication status.

---

# 21. Export Contract

## 21.1 Schema version

MVP:

```text
focus-reward-export-v1
```

## 21.2 Envelope

```json
{
  "schemaVersion": "focus-reward-export-v1",
  "exportType": "SKILL_SUBTREE",
  "generatedAt": "2026-10-28T12:00:00Z",
  "timezone": "Europe/Berlin",
  "data": {}
}
```

## 21.3 Export types

```text
SKILL
SKILL_SUBTREE
PATH
ACCOUNT
ANALYSIS
```

`ANALYSIS` is a privacy-filtered variant of the requested scope.

## 21.4 Skill/subtree data

Include where relevant:

- skill identity within export;
- path name;
- hierarchy;
- current + historical goals;
- prerequisites;
- mastery configuration;
- criteria;
- attempts;
- quests;
- focus history or summarized focus;
- XP/time;
- maintenance routines/completions;
- reward eligibility.

## 21.5 Account export

May include account-level:

- email;
- display name;
- timezone;
- paths;
- all progression;
- reward ledger.

Do not include:
- secrets;
- auth session identifiers;
- Google tokens.

## 21.6 Analysis export

Exclude:

- google_subject;
- email;
- display_name;
- avatar_url;
- auth/session data.

Use stable export-local IDs so graph relationships remain understandable.

## 21.7 Export IDs

Database UUIDs may be emitted for personal backup export.

For analysis export, use generated local IDs if desired:

```text
skill-1
skill-2
goal-1
```

This reduces unnecessary correlation.

---

# 22. Feedback

Fields:

| Field | Type |
|---|---|
| id | UUID |
| user_id | UUID FK nullable after account deletion/anonymization |
| message | Text |
| created_at | Instant |

Feedback is intentionally simple.

No voting, issue tracker, or support system in MVP.

---

# 23. Authentication and Authorization

## 23.1 Authentication

Use Spring Security OAuth2/OIDC Google login.

Recommended web model:

```text
Google OIDC
   ↓
Backend callback
   ↓
local UserAccount
   ↓
server-side authenticated session
   ↓
Secure HttpOnly cookie
```

No JWT is required for MVP unless deployment architecture creates a compelling reason.

## 23.2 Session cookie

Production:

- Secure;
- HttpOnly;
- appropriate SameSite setting;
- reasonable expiration;
- server-side session invalidation on logout/account deletion.

## 23.3 CSRF

If cookie-based authentication is used, state-changing requests must have CSRF protection.

## 23.4 Authorization

Never trust user/path IDs from the client by themselves.

Typical service check:

```text
load resource
verify resource.user_id == authenticated_user.id
```

Admin endpoints additionally require:

```text
role = ADMIN
```

## 23.5 Authorization matrix

| Resource | USER | ADMIN |
|---|---:|---:|
| Own paths/skills | CRUD | CRUD own |
| Other user's progression | No | No by default |
| Public templates | Read/fork | Read/fork |
| Template drafts | No | CRUD |
| Publish template | No | Yes |
| Own exports | Yes | Yes own |
| Own account deletion | Yes | Yes own |

Admin role does not automatically mean unrestricted access to private user progression.

---

# 24. Transaction Boundaries

## 24.1 StartFocus

One transaction:

1. normalize user's stale ACTIVE session;
2. ensure no ACTIVE session remains;
3. validate target skill/goal/maintenance/prerequisites;
4. snapshot timezone/reward eligibility;
5. insert ACTIVE session;
6. commit.

Database backstop:

partial unique index:

```sql
UNIQUE(user_id)
WHERE status = 'ACTIVE'
```

## 24.2 FinishFocus

One transaction:

1. lock focus row;
2. handle idempotent terminal state;
3. calculate elapsed;
4. if >10h:
   - REVIEW_REQUIRED;
   - commit;
5. else:
   - set confirmed duration;
   - COMPLETED;
   - recalc reward day;
   - commit.

XP requires no separate write.

## 24.3 CorrectFocus

One transaction:

1. lock completed/review session;
2. update confirmed duration;
3. set COMPLETED;
4. recalc reward day;
5. commit.

## 24.4 SpendReward

One serializable/locked transaction:

1. lock account/reward serialization point;
2. calculate balance;
3. validate;
4. insert negative spend;
5. commit.

## 24.5 PassMastery

One transaction as described in Section 12.

## 24.6 PublishTemplateRevision

One transaction:

1. lock template;
2. validate draft;
3. mark draft PUBLISHED;
4. set publish timestamp;
5. point template to revision;
6. commit.

## 24.7 ForkTemplate

One transaction.

No partially forked graph is allowed.

---

# 25. Concurrency and Idempotency

## 25.1 Focus start race

Two tabs may attempt to start simultaneously.

Protection:

- application check;
- partial unique database index on ACTIVE session.

One succeeds.

Other receives conflict.

## 25.2 Focus finish race

Two finish requests may arrive.

Lock session row.

If already COMPLETED:

return current completed representation rather than awarding again.

## 25.3 Reward recalculation race

Lock:

```text
RewardDailyAccrual(user_id, local_date)
```

before recalculation.

If row does not yet exist, use safe insert/upsert/retry strategy.

## 25.4 Spend race

Serialize spends using locked user/reward row before balance check.

## 25.5 Template publish race

Lock template and verify draft version.

Optimistic version prevents lost admin edits.

---

# 26. Database Constraints and Indexes

The exact migration syntax belongs in implementation, but these constraints are required.

## 26.1 User

```text
UNIQUE(google_subject)
INDEX(email)
```

## 26.2 Skill

```text
INDEX(path_id)
INDEX(parent_id)
INDEX(path_id, archived_at)
```

Hierarchy-cycle validation remains service/domain logic.

## 26.3 Goal

Partial unique:

```text
UNIQUE(skill_id)
WHERE status = 'ACTIVE'
```

No uniqueness constraint applies to `PLANNED`; multiple future goals are allowed.

Indexes:

```text
INDEX(skill_id, status)
```

## 26.4 Prerequisite

```text
UNIQUE(skill_id, prerequisite_skill_id, type, required_mastery_level)
INDEX(prerequisite_skill_id)
```

Handle nullable uniqueness carefully for GOAL_ACHIEVED, possibly with separate normalized key or partial indexes.

## 26.5 Mastery criterion

```text
UNIQUE(skill_id, level)
```

## 26.6 Focus

```text
INDEX(user_id, started_at DESC)
INDEX(skill_id, started_at DESC)
INDEX(user_id, reward_local_date)
```

Partial unique:

```text
UNIQUE(user_id)
WHERE status = 'ACTIVE'
```

## 26.7 Reward daily accrual

```text
UNIQUE(user_id, local_date)
```

## 26.8 Reward transaction

```text
INDEX(user_id, created_at DESC)
INDEX(user_id, local_date)
```

## 26.9 Maintenance

Partial unique active routine per skill:

```text
UNIQUE(skill_id)
WHERE status = 'ACTIVE'
```

Completion:

```text
UNIQUE(routine_id, completed_on)
```

## 26.10 Template revision

```text
UNIQUE(template_id, revision_number)
INDEX(template_id, status)
```

---

# 27. REST API Surface

Base:

```text
/api/v1
```

Exact DTO fields may be refined, but responsibilities should remain stable.

## 27.1 Current user

```text
GET    /me
PATCH  /me
DELETE /me
```

Google login/logout routes may be Spring Security-managed.

## 27.2 Paths

```text
POST   /paths
GET    /paths
GET    /paths/{pathId}
PATCH  /paths/{pathId}
POST   /paths/{pathId}/archive
```

## 27.3 Skills

```text
POST   /paths/{pathId}/skills
GET    /paths/{pathId}/skills
GET    /skills/{skillId}
PATCH  /skills/{skillId}
POST   /skills/{skillId}/archive
```

## 27.4 Goals

```text
POST   /skills/{skillId}/goals
GET    /skills/{skillId}/goals
POST   /goals/{goalId}/activate
POST   /goals/{goalId}/achieve
POST   /goals/{goalId}/replace
POST   /goals/{goalId}/abandon
PATCH  /goals/{goalId}
```

## 27.5 Prerequisites

```text
POST   /skills/{skillId}/prerequisites
GET    /skills/{skillId}/prerequisites
DELETE /prerequisites/{id}
```

## 27.6 Mastery

```text
PUT    /skills/{skillId}/mastery
PUT    /skills/{skillId}/mastery/criteria/{level}
POST   /skills/{skillId}/mastery/attempts
GET    /skills/{skillId}/mastery/attempts
```

Request to attempt contains:

```text
result = PASS | FAIL
evidenceNote?
evidenceUrl?
```

Target level is server-derived (`current + 1`) rather than trusted from client.

## 27.7 Quests

```text
POST   /skills/{skillId}/quests
GET    /skills/{skillId}/quests
PATCH  /quests/{questId}
POST   /quests/{questId}/complete
POST   /quests/{questId}/archive
```

## 27.8 Focus

```text
POST   /focus-sessions
GET    /focus-sessions/current
GET    /focus-sessions
GET    /focus-sessions/{id}
POST   /focus-sessions/{id}/finish
POST   /focus-sessions/{id}/review
POST   /focus-sessions/{id}/discard
PATCH  /focus-sessions/{id}
```

`PATCH` on COMPLETED is limited to editable note/confirmed duration.

## 27.9 Progress

```text
GET /progress/character
GET /skills/{skillId}/progress
GET /paths/{pathId}/progress
```

Character response includes:

- lifetime XP;
- lifetime focus seconds/minutes;
- level;
- current level threshold;
- next level threshold;
- progress percentage.

## 27.10 Rewards

```text
GET  /rewards
GET  /rewards/transactions
POST /rewards/spends
```

`GET /rewards` returns:

- balance;
- today's eligible minutes;
- today's earned reward;
- daily cap;
- minutes to next €1 if cap not reached.

## 27.11 Maintenance

```text
PUT    /skills/{skillId}/maintenance
GET    /skills/{skillId}/maintenance
POST   /maintenance/{routineId}/complete
POST   /maintenance/{routineId}/pause
POST   /maintenance/{routineId}/resume
POST   /maintenance/{routineId}/archive
```

## 27.12 Templates — public

```text
GET  /templates
GET  /templates/{slug}
POST /templates/{templateId}/fork
```

Fork request may include user-confirmed/editable suggested goals.

## 27.13 Templates — admin

```text
POST   /admin/templates
POST   /admin/templates/{templateId}/revisions
GET    /admin/template-revisions/{revisionId}
PUT    /admin/template-revisions/{revisionId}
POST   /admin/template-revisions/{revisionId}/validate
POST   /admin/template-revisions/{revisionId}/publish
POST   /admin/template-revisions/import
```

## 27.14 Export

```text
POST /exports/skill/{skillId}
POST /exports/subtree/{skillId}
POST /exports/path/{pathId}
POST /exports/account
```

Request option:

```text
mode = FULL | ANALYSIS
```

MVP may generate the JSON synchronously.

## 27.15 Feedback

```text
POST /feedback
```

---

# 28. HTTP Error Contract

Use Spring's `ProblemDetail` / `application/problem+json`.

Recommended fields:

```json
{
  "type": "https://.../problems/focus-already-active",
  "title": "Focus session already active",
  "status": 409,
  "code": "FOCUS_ALREADY_ACTIVE",
  "detail": "Finish or discard the current session first.",
  "instance": "/api/v1/focus-sessions"
}
```

## 28.1 Error code examples

```text
AUTH_REQUIRED
FORBIDDEN
RESOURCE_NOT_FOUND

INVALID_TIMEZONE
VALIDATION_FAILED

SKILL_NOT_TRAINABLE
SKILL_LOCKED
ACTIVE_GOAL_REQUIRED
ACTIVE_GOAL_ALREADY_EXISTS
INVALID_SKILL_HIERARCHY
SKILL_HIERARCHY_CYCLE
PREREQUISITE_CYCLE
INVALID_PREREQUISITE

MASTERY_DISABLED
MASTERY_CRITERION_MISSING
INVALID_MASTERY_TRANSITION

FOCUS_ALREADY_ACTIVE
FOCUS_NOT_ACTIVE
FOCUS_REVIEW_REQUIRED
INVALID_FOCUS_DURATION

INSUFFICIENT_REWARD_BALANCE

INVALID_MAINTENANCE_CADENCE

TEMPLATE_VALIDATION_FAILED
TEMPLATE_REVISION_NOT_DRAFT
```

## 28.2 HTTP mapping

General:

- 400 — malformed/basic validation;
- 401 — unauthenticated;
- 403 — authenticated but unauthorized;
- 404 — owned/public resource not found;
- 409 — valid request conflicts with current domain state;
- 422 — optional for semantically invalid structured import/template content;
- 500 — unexpected server failure only.

---

# 29. Validation Rules

Representative hard limits to prevent pathological input.

These are implementation defaults, not product identity.

```text
Path name                 1..120 chars
Skill name                1..120
Skill description         <= 4000
Goal title                1..160
Goal criterion            1..4000
Quest title               1..180
Quest description         <= 4000
Maintenance minimum action 1..240
Cue text                  <= 240
Focus note                <= 4000
Feedback                  1..10000
Evidence URL              valid http/https URL
Timezone                  valid ZoneId
```

Template JSON should have MVP safety bounds such as:

```text
max nodes per template: 250
max prerequisite edges: 500
max starter quests: 1000
```

These can be increased later.

---

# 30. Account Deletion

## 30.1 User flow

1. user requests deletion;
2. explicit confirmation;
3. invalidate active auth sessions;
4. transactional/controlled deletion or anonymization.

## 30.2 User-owned progression

Delete:

- paths;
- skills;
- goals;
- prerequisites;
- mastery;
- quests;
- focus sessions;
- maintenance;
- reward data.

## 30.3 Feedback

Either:

- delete;
- or anonymize `user_id = NULL`.

MVP preference: delete user-linked feedback unless there is a concrete need to retain it.

## 30.4 Templates authored by admin

Published global templates should not disappear because author account disappears.

If necessary:

```text
created_by = NULL
```

while published content remains.

## 30.5 Backups

Privacy notice must explain that deleted data may remain in encrypted backups until normal backup expiration.

---

# 31. Privacy and Minimal Tracking

MVP should not add:

- ad trackers;
- marketing pixels;
- behavioral analytics SDKs.

Necessary storage may include:

- authentication/session cookie;
- security/CSRF mechanisms;
- essential preference state.

Product-domain metrics are computed from first-party records.

Google login privacy disclosure must explain what identity fields are retained.

Export and deletion must be accessible from account settings.

---

# 32. Observability

Minimum production observability:

## Logs

Structured logs for:

- authentication failures;
- focus state transitions;
- focus review/correction;
- reward recalculation failures;
- template publication failures;
- export failures;
- authorization denials where useful.

Do not log:

- full OAuth tokens;
- session cookies;
- sensitive note/evidence contents unnecessarily.

## Metrics / health

At minimum:

- application health;
- DB connectivity;
- HTTP error rate;
- request latency;
- count of failed focus finalizations.

No user-facing analytics dashboard is required.

---

# 33. Domain Service Responsibilities

Recommended pure/application services:

## SkillTreeValidator

Validates:

- hierarchy;
- leaf/category rules;
- cycles.

## PrerequisiteEvaluator

Returns whether all prerequisite conditions are satisfied.

## CharacterLevelCalculator

Pure XP → level/threshold calculation.

## FocusSessionService

Owns focus state machine.

## RewardCalculator / RewardService

Owns daily reward formula, recalculation, ledger.

## MasteryService

Owns sequential mastery attempts.

## MaintenanceService

Owns cadence evaluation/completion.

## TemplateDocumentValidator

Validates `template-v1` document.

## TemplateForkService

Copies validated template document into user domain.

## ExportService

Produces `focus-reward-export-v1`.

These responsibilities should not be embedded in controllers.

---

# 34. Repository Boundaries

Suggested repositories:

```text
UserAccountRepository
PersonalPathRepository
SkillRepository
SkillGoalRepository
SkillPrerequisiteRepository
MasteryCriterionRepository
MasteryAttemptRepository
QuestRepository
FocusSessionRepository
RewardDailyAccrualRepository
RewardTransactionRepository
MaintenanceRoutineRepository
MaintenanceCompletionRepository
TemplateRepository
TemplateRevisionRepository
FeedbackRepository
```

Avoid generic "save everything" repositories.

Repository methods that participate in race-sensitive operations should expose explicit locking variants, e.g.:

```text
findByIdForUpdate(...)
findActiveByUserForUpdate(...)
findDailyAccrualForUpdate(...)
```

---

# 35. Persistence Relationship Summary

```text
UserAccount 1 ─── * PersonalPath
PersonalPath 1 ─── * SkillNode

SkillNode 1 ─── * SkillGoal
SkillNode 1 ─── * MasteryCriterion
SkillNode 1 ─── * MasteryAttempt
SkillNode 1 ─── * Quest
SkillNode 1 ─── * FocusSession

SkillNode * ─── * SkillNode
        via SkillPrerequisite

SkillNode 1 ─── 0..1 ACTIVE MaintenanceRoutine
MaintenanceRoutine 1 ─── * MaintenanceCompletion

UserAccount 1 ─── * RewardDailyAccrual
UserAccount 1 ─── * RewardTransaction

Template 1 ─── * TemplateRevision
```

---

# 36. Deletion / Referential Behavior

Recommended rules:

## Path archive

Does not cascade delete history.

## Skill archive

Keeps:
- goals;
- focus;
- mastery;
- quests;
- maintenance history.

Archived skill cannot receive new focus.

## Prerequisite pointing to archived skill

Before archiving a skill, user must resolve dependent active prerequisite edges, or service may archive/remove those edges explicitly with confirmation.

Do not silently change unlock logic.

## Quest archive

Historical focus may continue referencing it.

## Goal close

Never deletes focus or quests.

## Full account deletion

May cascade hard-delete all personal progression records.

---

# 37. Suggested Flyway Migration Order

This is a logical order, not required filenames.

## M1 — users

- user_account

## M2 — path/skill

- personal_path
- skill_node

## M3 — goals/prerequisites

- skill_goal
- skill_prerequisite

## M4 — mastery/quest

- mastery_criterion
- mastery_attempt
- quest

## M5 — focus

- focus_session
- partial unique active-session index

## M6 — reward

- reward_daily_accrual
- reward_transaction

## M7 — maintenance

- maintenance_routine
- maintenance_completion

## M8 — templates

- template
- template_revision

## M9 — feedback / final indexes

- feedback
- performance indexes/constraints

Do not create all tables in one giant migration if the project is being learned/built incrementally.

---

# 38. Testing Strategy

## 38.1 Unit tests — pure logic

Mandatory:

### CharacterLevelCalculator

- L1 threshold 0;
- L100 threshold 600000;
- known thresholds;
- monotonic levels;
- XP > 600000 remains L100.

### Reward calculation

- 0/19/20/39/40/99/100/101 minutes;
- daily cap;
- correction positive delta;
- correction negative delta.

### PrerequisiteEvaluator

- no prerequisites;
- goal condition;
- mastery condition;
- mixed AND conditions.

### Maintenance cadence

All cadence modes and boundary dates.

### Template validation

- cycles;
- missing refs;
- invalid leaf/category;
- duplicate keys.

## 38.2 Integration tests — PostgreSQL/Testcontainers

Mandatory:

- unique ACTIVE goal;
- unique ACTIVE focus session;
- focus start race;
- duplicate focus finish;
- >10h review transition;
- correction changes XP;
- reward recalculation transaction;
- concurrent spends cannot overdraw;
- ownership isolation;
- template fork atomicity;
- template revision publish;
- account deletion;
- export generated from real persisted graph.

## 38.3 API/security tests

- unauthenticated rejection;
- user A cannot access user B resource;
- admin template route denied to USER;
- invalid state returns 409;
- validation errors use consistent ProblemDetail format.

---

# 39. MVP Implementation Sequence

The order should minimize cross-system complexity.

## Milestone 1 — Identity + tree + goal

Implement:

- Google login;
- UserAccount/timezone;
- Path;
- Skill;
- category/trainable invariants;
- Goal;
- basic tree view API.

Usable outcome:

> real user can sign in and define what they are trying to achieve.

## Milestone 2 — Focus + XP + level

Implement:

- focus state machine;
- correction;
- REVIEW_REQUIRED;
- XP derivation;
- character level calculator.

Usable outcome:

> user can actually train and see progression.

## Milestone 3 — Reward

Implement:

- reward eligibility;
- daily accrual;
- ledger;
- spend.

Usable outcome:

> original reward loop works.

## Milestone 4 — Dependencies + mastery + quests

Implement:

- prerequisite graph;
- mastery optionality;
- criteria/attempts;
- quests.

Usable outcome:

> progression tree has capability logic.

## Milestone 5 — Maintenance

Implement routines/cadence.

## Milestone 6 — Templates/admin/fork

Implement:

- JSONB template revision;
- validation;
- admin CRUD;
- fork.

## Milestone 7 — Export/privacy/beta hardening

Implement:

- JSON exports;
- analysis mode;
- account deletion;
- feedback;
- observability;
- backups;
- responsive/error-state cleanup.

Then founder dogfood → external beta.

---

# 40. Decisions Explicitly Deferred

These do not block implementation.

## Branding

Final name/domain.

## Frontend technology

BRD does not require one.

## Fancy tree visualization

Nested/indented tree is sufficient for MVP.

## Passive long-session notification

Can be added if dogfooding shows it helps.

## Template merge/update

Post-MVP.

## AI generation

Post-MVP; must produce the same validated template draft contract.

## Titles/cosmetics/achievements

Post-MVP.

## Social/community templates

Post-MVP.

## More sophisticated cross-midnight reward splitting

MVP assigns reward to session start local date.

If real behavior shows this is materially wrong, change later.

---

# 41. Decisions That Must Not Drift During Implementation

The following are intentional and should not be casually "simplified" in code:

1. XP is derived from confirmed focus, not manually incremented balances.
2. Goal completion never follows automatically from XP.
3. Mastery is optional and independent of XP.
4. Category/parent skills never receive direct training XP.
5. Forgotten timers do not receive automatic credit after 10 hours.
6. User is trusted to correct their own time.
7. Reward is separate from XP.
8. Historical reward eligibility is snapshotted.
9. Reward is capped per local calendar day.
10. Forks never silently follow template edits.
11. Published template document history is preserved.
12. AI is not required for the MVP to work.
13. User private data is not exposed merely because an account is ADMIN.
14. External beta usability/reliability matters more than building RPG decoration.

---

# 42. Implementation Readiness

After this specification, the next artifact should be an **Implementation Plan / Epic Breakdown**, not another conceptual product document.

The implementation plan should convert this solution into:

- epics;
- vertical slices;
- entity/migration tasks;
- API tasks;
- integration tests;
- frontend screens;
- beta release gate.

Before coding each milestone, only milestone-local details should be resolved.

The domain model itself is sufficiently specified to begin Spring Boot entities and Flyway migrations.

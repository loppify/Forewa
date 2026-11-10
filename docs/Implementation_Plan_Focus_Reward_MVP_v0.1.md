# Implementation Plan / Epic Breakdown
## Focus Reward — MVP

**Document version:** 0.1  
**Date:** 2026-10-29  
**Source documents:**  
- `BRD_Focus_Reward_MVP_v0.3.md`
- `Domain_Solution_Spec_Focus_Reward_v0.2.md`

---

# 1. Goal

Convert the approved MVP domain into an implementation sequence that:

- produces usable vertical slices early;
- keeps migrations/domain rules/API/tests aligned;
- avoids building all entities first and behavior later;
- preserves the MVP's product-validation purpose;
- creates a beta-ready multi-user product rather than a personal prototype.

The implementation order is intentionally domain-first and backend-heavy because the product's differentiation depends on correct state transitions and progression rules.

---

# 2. Delivery Strategy

Each milestone should end with a **usable capability**, not only created tables/classes.

Every vertical slice should include, where applicable:

1. migration;
2. JPA model;
3. repository;
4. application/domain service;
5. DTOs + validation;
6. controller/API;
7. integration tests;
8. minimal frontend workflow;
9. error/loading/empty states;
10. manual acceptance check.

Do not implement the entire persistence layer before exposing any behavior.

---

# 3. Definition of Done for a Feature

A feature is done only when:

- schema migration exists;
- domain invariants are enforced server-side;
- happy path works through API;
- ownership/authorization is enforced;
- expected conflicts return explicit error codes;
- integration tests cover critical state transitions;
- UI can complete the intended user action;
- data survives refresh/relogin;
- no known critical consistency defect remains.

A repository/entity alone is not a completed feature.

---

# 4. Epic 0 — Project Foundation

## Objective

Create the production-capable Spring foundation without prematurely implementing product behavior.

## Backend tasks

### 0.1 Bootstrap

Create Spring Boot project with:

- Java 21+
- Gradle
- Spring Web
- Spring Security
- OAuth2 Client
- Spring Data JPA
- PostgreSQL driver
- Flyway
- Jakarta Validation
- Actuator
- Testcontainers
- JUnit

### 0.2 Environments

Define:

```text
local
test
production
```

Configuration comes from environment variables/secrets.

### 0.3 PostgreSQL local development

Docker Compose for PostgreSQL.

No application container is required initially unless useful.

### 0.4 Flyway

Create empty migration chain baseline.

Rule:

> Hibernate schema generation must not manage production schema.

Use validation rather than auto-create/update.

### 0.5 Error foundation

Create:

- global exception handling;
- Spring `ProblemDetail`;
- stable domain `code` field;
- validation error representation.

### 0.6 Common persistence conventions

Implement standard:

- UUID IDs;
- timestamps;
- optimistic version;
- UTC handling.

Avoid an inheritance hierarchy so elaborate that it obscures entities.

### 0.7 Test infrastructure

Create Testcontainers PostgreSQL base/infrastructure.

First test:

- application context;
- migration succeeds;
- database reachable.

## Exit criterion

A blank authenticated-capable application can boot locally and under integration tests with a real PostgreSQL container.

---

# 5. Epic 1 — Google Identity and User Boundary

## Objective

A real external user can authenticate and own isolated data.

## Migration

Create:

```text
user_account
```

Fields per Domain Spec.

Indexes:

```text
UNIQUE google_subject
INDEX email
```

## Backend

### 1.1 Google OIDC

Implement Spring Security Google authentication.

Use Google `sub` as identity key.

### 1.2 Local account provisioning

On successful first login:

- create UserAccount;
- save minimal allowed profile fields.

On later login:

- find by `google_subject`;
- update allowed display metadata where appropriate;
- update `last_login_at`.

### 1.3 Session model

Use server-side authenticated web session + Secure/HttpOnly cookie.

### 1.4 `/api/v1/me`

Implement:

```text
GET /me
PATCH /me
```

PATCH initially supports timezone.

### 1.5 Timezone

Validate `ZoneId`.

Frontend proposes browser timezone.

### 1.6 Authorization utility

Create one consistent way to obtain authenticated local user ID.

Do not accept user ID in normal user-scoped mutation requests.

## Tests

- first login creates account;
- repeat login reuses account;
- email change does not duplicate user;
- invalid timezone rejected;
- unauthenticated `/me` rejected.

## Frontend minimum

- Login with Google.
- Initial timezone confirmation.
- Logged-in shell.
- Sign out.

## Exit criterion

Two different Google-backed test users are isolated and have independent application state.

---

# 6. Epic 2 — Paths, Skills, and Goal Queue

## Objective

User can model what they want to develop, including future/planned goals.

## Migrations

Create:

```text
personal_path
skill_node
skill_goal
```

Important DB constraint:

```text
UNIQUE(skill_id)
WHERE goal.status = ACTIVE
```

No uniqueness for PLANNED.

## Domain

### 2.1 Path

CRUD/archive.

### 2.2 Skill hierarchy

Implement:

```text
CATEGORY
TRAINABLE
```

Rules:

- CATEGORY can have children;
- TRAINABLE is leaf;
- cannot add child to TRAINABLE without valid conversion;
- cannot focus CATEGORY.

### 2.3 Skill tree validator

Implement cycle protection for parent hierarchy.

### 2.4 Goal statuses

```text
PLANNED
ACTIVE
ACHIEVED
REPLACED
ABANDONED
```

### 2.5 Create goal

Allow:

- direct ACTIVE if no active goal exists;
- PLANNED regardless of other planned goals.

### 2.6 Activate planned goal

Operation:

```text
POST /goals/{id}/activate
```

Fails with 409 if another ACTIVE goal exists.

### 2.7 Close active goal

Operations:

```text
achieve
replace
abandon
```

### 2.8 Goal history

GET all goals ordered by lifecycle/time.

## APIs

Implement:

```text
POST   /paths
GET    /paths
GET    /paths/{id}
PATCH  /paths/{id}

POST   /paths/{id}/skills
GET    /paths/{id}/skills
GET    /skills/{id}
PATCH  /skills/{id}

POST   /skills/{id}/goals
GET    /skills/{id}/goals
POST   /goals/{id}/activate
POST   /goals/{id}/achieve
POST   /goals/{id}/replace
POST   /goals/{id}/abandon
PATCH  /goals/{id}
```

## Tests

Mandatory:

- multiple PLANNED goals allowed;
- maximum one ACTIVE;
- planned goal activation succeeds when no active;
- planned activation conflicts when active exists;
- closing active permits activation of planned;
- closed goal transition rejected;
- hierarchy cycle rejected;
- TRAINABLE cannot have child;
- cross-user access rejected.

## Frontend

Minimum screens:

### Paths
- list;
- create.

### Path detail
- nested skill tree/list;
- category/trainable distinction.

### Skill detail
- current goal;
- planned goals;
- closed history;
- create/edit/activate goal.

## Exit criterion

User can represent:

```text
Handstand
Current: 30 sec freestanding hold
Planned:
- 10 m handstand walk
- 1 strict HSPU
```

without losing goal history.

---

# 7. Epic 3 — Focus Stopwatch, XP, and Character Level

## Objective

The first real daily-use core loop works.

## Migration

Create:

```text
focus_session
```

Add partial unique active-session index.

## Domain

### 3.1 Focus status

```text
ACTIVE
REVIEW_REQUIRED
COMPLETED
DISCARDED
```

### 3.2 Start focus

Preconditions:

- TRAINABLE;
- unlocked structurally;
- active goal or active maintenance;
- no active session.

Snapshot:

- timezone;
- reward local date;
- reward eligibility.

Reward system itself can remain inactive until Epic 4.

### 3.3 Stopwatch

Backend stores `started_at`.

Frontend calculates visual elapsed duration from timestamp.

Do not write "ticks" to backend.

### 3.4 Finish ≤10h

Confirm elapsed duration.

Allow manual correction.

Persist:

- raw elapsed;
- confirmed seconds;
- credited minutes.

### 3.5 Finish >10h

Transition:

```text
ACTIVE → REVIEW_REQUIRED
```

No XP.

### 3.6 Review

User supplies actual time:

```text
0..600 minutes
```

Then COMPLETED.

Or discard.

### 3.7 Stale normalization

On current-session read/start:

if active elapsed >10h:

```text
→ REVIEW_REQUIRED
```

### 3.8 XP derivation

No XP ledger.

Queries/services derive:

```text
skill XP
character XP
category aggregate XP
```

from completed credited minutes.

### 3.9 CharacterLevelCalculator

Implement exact formula.

Return:

- current level;
- lifetime XP;
- current threshold;
- next threshold;
- XP into current level;
- XP needed to next;
- percentage.

## APIs

```text
POST /focus-sessions
GET  /focus-sessions/current
GET  /focus-sessions
GET  /focus-sessions/{id}

POST /focus-sessions/{id}/finish
POST /focus-sessions/{id}/review
POST /focus-sessions/{id}/discard
PATCH /focus-sessions/{id}

GET /progress/character
GET /skills/{id}/progress
GET /paths/{id}/progress
```

## Concurrency tests

Critical:

- two tabs start simultaneously;
- one ACTIVE only;
- duplicate finish awards once;
- refresh does not lose timer;
- >10h transitions to REVIEW_REQUIRED;
- review correction changes XP;
- discarded session gives zero XP.

## Level formula tests

Hard-code reference thresholds from Domain Spec.

## Frontend

Core HUD/dashboard:

- current active skill;
- stopwatch;
- Finish;
- Cancel;
- review-required recovery.

After finish show:

```text
duration
XP gained
skill total
character level/progress
```

Reward line can be absent until Epic 4.

## Exit criterion

The founder can use the application for actual work for several days without manually editing the DB.

This is the earliest meaningful dogfood point.

---

# 8. Epic 4 — Reward Economy

## Objective

Implement the original focus → personal reward loop correctly.

## Migrations

Create:

```text
reward_daily_accrual
reward_transaction
```

## Domain

### 4.1 Skill reward eligibility

Ensure TRAINABLE skill setting:

```text
reward_eligible
```

### 4.2 Snapshot

Focus session already snapshots reward eligibility.

### 4.3 Daily accrual

Implement:

```text
eligible_minutes
earned_cents
```

per user/local date.

### 4.4 Formula

```text
floor(minutes / 20) * €1
cap €5
```

### 4.5 Session correction

Recalculate day.

Insert positive/negative adjustment transaction.

Never rewrite ledger history.

### 4.6 Spend

Create spend operation.

Reject overdraft.

## APIs

```text
GET  /rewards
GET  /rewards/transactions
POST /rewards/spends
```

## Tests

Boundary cases:

```text
0
19
20
39
40
99
100
101
```

Also:

- separate sessions accumulate;
- non-eligible skill contributes zero;
- reward eligibility toggle does not affect old session;
- correction creates positive delta;
- correction creates negative delta;
- €5 cap;
- concurrent spend cannot overdraw;
- timezone date is stable after timezone change.

## Frontend

Reward panel:

```text
Balance
Today: €X / €5
Eligible minutes
Minutes to next €1
```

Spend dialog:

- amount;
- description.

## Exit criterion

User can earn, correct, and spend the reward balance without inconsistencies.

---

# 9. Epic 5 — Prerequisites, Optional Mastery, and Quests

## Objective

Turn the tree from organization into real progression logic.

## Migrations

Create:

```text
skill_prerequisite
mastery_criterion
mastery_attempt
quest
```

## Prerequisites

Implement types:

```text
GOAL_ACHIEVED
MASTERY_AT_LEAST
```

Validate:

- same path;
- no cycles;
- correct mastery configuration.

## Mastery

### 5.1 Enable/disable

Mastery optional.

Disabling after progression exists should require explicit handling.

MVP safe rule:

> once mastery attempts/current mastery >0 exist, disabling mastery is rejected unless history is explicitly reset.

Do not silently erase mastery.

### 5.2 Criteria

CRUD levels 1–4.

### 5.3 Attempt

Server derives next target level.

Persist criterion snapshot.

PASS increments.

FAIL records attempt only.

## Quests

CRUD.

Optional goal link.

No XP bonus.

## Derived state

Implement:

```text
LOCKED
AVAILABLE
TRAINING
ACHIEVED
MAINTENANCE
ARCHIVED
```

Do not persist as source-of-truth.

## Tests

- goal prerequisite;
- mastery prerequisite;
- AND of multiple prerequisites;
- cycle rejection;
- mastery disabled;
- missing criterion;
- sequential PASS;
- FAIL;
- criterion edit does not alter old snapshot;
- quest completion gives no XP;
- cross-user prerequisite rejected.

## Frontend

Skill detail expands:

- prerequisites;
- locked reasons;
- mastery toggle/criteria/current level;
- attempt mastery;
- quests.

Path tree shows:

- locked;
- available;
- training;
- achieved.

## Exit criterion

A user can create a progression chain such as:

```text
Pull-ups goal achieved
        ↓
Muscle-up becomes available
```

or:

```text
Spring Boot mastery >= Independent
        ↓
Spring Security becomes available
```

---

# 10. Epic 6 — Maintenance Routines

## Objective

Represent retained skills as habits without turning the entire product into a habit tracker.

## Migrations

Create:

```text
maintenance_routine
maintenance_completion
```

## Cadences

Implement:

```text
SELECTED_WEEKDAYS
TIMES_PER_WEEK
EVERY_N_DAYS
```

## Domain

### 6.1 Routine lifecycle

```text
ACTIVE
PAUSED
ARCHIVED
```

Maximum one ACTIVE per skill.

### 6.2 Direct completion

No XP.

### 6.3 Timed maintenance

User can start focus on maintenance skill without active training goal.

Normal XP/reward rules apply.

### 6.4 Due computation

Build pure cadence evaluator.

## Tests

Extensive date-boundary tests.

Use user timezone.

Test Monday/Sunday boundaries.

## Frontend

Maintenance view:

- minimum action;
- cadence;
- cue;
- current due state;
- completion button.

Dashboard can show due routines.

## Exit criterion

User can move an acquired skill to maintenance and use it for one complete recurrence cycle.

---

# 11. Epic 7 — Templates, Admin, Revision History, Fork

## Objective

Make onboarding usable for other people and create the future AI content seam.

## Migrations

Create:

```text
template
template_revision
```

JSONB document.

## Backend

### 7.1 Template document DTO/schema

Implement `template-v1`.

### 7.2 Validator

Validate:

- unique keys;
- references;
- cycles;
- node types;
- mastery prerequisites;
- content limits.

### 7.3 Admin role

Seed/configure first ADMIN.

Do not make ADMIN able to read user private progression by default.

### 7.4 Draft

Create/edit.

### 7.5 Publish

Immutable published revision.

### 7.6 Edit published

Clone to next draft revision.

### 7.7 Fork

Transactional copy into relational user domain.

Important onboarding question:

Suggested goals should not become ACTIVE blindly.

Flow:

```text
template suggested goal
→ user sees/edits/confirms
→ create ACTIVE or PLANNED personal goal
```

### 7.8 Structured import

Implement if effort remains modest.

JSON:

```text
parse → validate → draft
```

## Tests

- invalid graph;
- cycle;
- duplicate key;
- publish;
- edit creates revision;
- old revision unchanged;
- fork atomicity;
- fork user isolation;
- later publish does not change old fork.

## Frontend

Public template browser.

Preview.

Fork onboarding.

Admin:

- templates;
- revision editor;
- validation errors;
- publish.

Do not build visual graph editor unless trivial.

Nested structured forms/JSON-assisted editor are acceptable for MVP admin.

## Exit criterion

A new user can sign in, select a founder-created template, personalize its goal, and begin focus without manually building a tree.

---

# 12. Epic 8 — Export and Data Ownership

## Objective

Make the user's progression portable and usable in external AI analysis.

## Backend

Implement:

```text
focus-reward-export-v1
```

Types:

```text
SKILL
SKILL_SUBTREE
PATH
ACCOUNT
ANALYSIS
```

## Serializer

Use dedicated export DTOs.

Do not serialize JPA entities directly.

## Analysis mode

Remove:

- Google sub;
- email;
- display name;
- avatar.

Relationships still work via export-local IDs.

## APIs

```text
POST /exports/skill/{id}
POST /exports/subtree/{id}
POST /exports/path/{id}
POST /exports/account
```

For MVP synchronous JSON download is enough.

## Tests

- correct subtree boundaries;
- complete graph refs;
- no other-user data;
- analysis mode strips identity;
- version included;
- export remains valid after archived historical content.

## Frontend

Download actions:

- skill;
- subtree;
- path;
- account;
- "Export for AI analysis".

## Exit criterion

User can export their actual tree, paste analysis version into an external LLM, and the structure remains intelligible.

---

# 13. Epic 9 — Account Deletion, Feedback, Privacy, Hardening

## Objective

Reach external-beta quality.

## Account deletion

Implement:

- confirmation;
- session invalidation;
- personal-data deletion;
- template author preservation/anonymization if admin;
- feedback policy.

## Feedback

Simple form.

## Privacy UX

Add:

- Privacy page;
- account/export/delete controls;
- Google identity disclosure;
- cookie/storage explanation.

## Observability

Add/verify:

- health endpoint;
- structured logging;
- errors;
- focus finalization/reward failure logging.

## Backups

Configure practical database backup strategy for production.

## Error-state audit

All MVP screens need:

- loading;
- empty;
- validation;
- authorization;
- conflict;
- unexpected failure.

## Responsive audit

Desktop first is fine, but core workflows must function on mobile browser.

## Exit criterion

Release Acceptance Gate from BRD v0.3 passes.

---

# 14. Epic 10 — Founder Dogfood

## Objective

Find product/domain failures before asking others to use it.

## Minimum duration

7 active-use days.

## Required scenarios

Founder must actually use:

- at least 2 paths;
- multiple active/planned goals;
- reward-eligible and non-eligible skills;
- a forgotten session intentionally forced into REVIEW_REQUIRED;
- a manual correction;
- at least one mastery-enabled skill;
- one mastery-disabled performance skill;
- prerequisite unlock;
- maintenance routine;
- reward spend;
- export for AI analysis;
- template fork.

## Bug severity

### Blocker

- data loss;
- ownership leak;
- wrong reward balance;
- duplicate XP/reward;
- broken focus recovery;
- impossible core workflow.

Must fix before beta.

### Major

- user can complete workflow only via awkward workaround;
- confusing domain state;
- frequent 500/error.

Should fix before beta.

### Minor

Cosmetic/low-friction issue.

Can ship if documented.

---

# 15. Epic 11 — Closed Beta

## Objective

Test whether the product is worth further investment.

## Cohort

~10–20 meaningful users.

Sources:

- acquaintances;
- friends;
- founder media/audience if available.

## Duration

14 days.

## Beta operation

Do not teach every user personally.

Provide:

- short onboarding text;
- feedback action;
- contact route for bugs.

## Review

At end evaluate BRD metrics and qualitative questions.

Decision:

```text
GO
ITERATE
STOP/PIVOT
```

Only after this decision should post-MVP AI/RPG expansion be planned.

---

# 16. Backend Package Sequence

Suggested code growth:

```text
com.focusreward
├── auth
├── user
├── path
├── skill
│   ├── goal
│   ├── prerequisite
│   └── mastery
├── quest
├── focus
├── reward
├── maintenance
├── template
├── export
├── feedback
└── common
```

Do not create one package per database table if the behavior belongs to a single domain area.

---

# 17. Migration Map

Suggested Flyway sequence:

```text
V001__user_account.sql
V002__personal_path_and_skill.sql
V003__skill_goal.sql
V004__focus_session.sql
V005__reward.sql
V006__prerequisite_mastery_quest.sql
V007__maintenance.sql
V008__template.sql
V009__feedback.sql
V010__hardening_indexes.sql
```

Exact numbering may differ.

Avoid premature tables for post-MVP features.

---

# 18. Minimum Frontend Surface

MVP does not need dozens of routes.

Suggested surface:

```text
/
  → dashboard

/login

/paths
/paths/{id}

/skills/{id}

/focus
  current focus overlay/widget may be global

/rewards

/maintenance

/templates
/templates/{slug}

/settings
  profile
  timezone
  export
  privacy
  delete account

/admin/templates
/admin/templates/{id}

/feedback
```

Dashboard should answer:

- What am I training?
- Is focus running?
- What goal am I moving toward?
- What maintenance is due?
- How much reward balance do I have?
- What level am I?

---

# 19. UI Priority

## P0

- clarity;
- state correctness;
- fast focus start/finish;
- goal visibility;
- recovery from mistakes.

## P1

- game-like dark aesthetic;
- XP bars;
- level display;
- tree states;
- satisfying lightweight transitions.

## P2

- elaborate animation;
- skill constellation;
- cosmetic systems.

Do not delay beta for P2.

---

# 20. Technical Risk Order

Implement/test these early because they have highest consequence:

1. ownership isolation;
2. focus idempotency;
3. active-session concurrency;
4. reward recalculation;
5. goal uniqueness;
6. prerequisite cycles;
7. template fork atomicity;
8. export privacy.

Do not spend early engineering effort on:

- CSS polish;
- animation;
- title/rank systems;
- AI generation.

---

# 21. Recommended First Coding Sprint

Do **not** start by implementing the entire plan.

First implementation sprint should cover only:

## Sprint 1

### Foundation
- Spring project;
- PostgreSQL;
- Flyway;
- Testcontainers;
- ProblemDetail.

### User
- temporary dev auth or Google auth depending setup speed;
- UserAccount;
- timezone.

### Path
- PersonalPath.

### Skill
- CATEGORY/TRAINABLE;
- hierarchy.

### Goal
- PLANNED/ACTIVE/closed lifecycle;
- one ACTIVE constraint.

### Minimal API/UI
User can:

```text
sign in
→ create path
→ create category/trainable skill
→ create multiple planned goals
→ activate one
→ see tree
```

Nothing else.

## Sprint 1 exit test

Without manual database edits:

1. create `Calisthenics`;
2. create `Pull-ups`;
3. create:
   - ACTIVE goal = `10 strict pull-ups`;
   - PLANNED goal = `muscle-up prerequisite strength target`;
4. attempt second ACTIVE → receive conflict;
5. achieve first;
6. activate planned;
7. relogin;
8. state is intact.

Only then begin focus.

---

# 22. Requirements Traceability to Implementation

| Domain capability | Epic |
|---|---:|
| Google auth / ownership | 1 |
| Paths / skills | 2 |
| Planned + active goals | 2 |
| Focus stopwatch | 3 |
| XP / level | 3 |
| Reward economy | 4 |
| Prerequisites | 5 |
| Optional mastery | 5 |
| Quests | 5 |
| Maintenance | 6 |
| Templates / revisions / forks | 7 |
| Structured import | 7 |
| Export | 8 |
| Account deletion / privacy | 9 |
| Dogfood | 10 |
| External validation | 11 |

---

# 23. MVP Stop Rule During Development

If implementation expands substantially beyond the named epics, stop and compare the new feature against BRD scope.

Examples that require explicit post-MVP deferral unless truly necessary:

- AI integration;
- social profiles;
- friends;
- achievements;
- title system;
- cosmetics;
- complex ranks;
- skill-decay percentages;
- mobile-native app;
- payments;
- public marketplace;
- advanced analytics;
- fancy graph rendering.

The objective is not to build the final vision.

The objective is to reach reliable external use quickly enough to learn whether the model deserves the final vision.

---

# 24. Next Immediate Action

Begin **Sprint 1 / Epic 0–2 slice**.

The first code-facing artifact after this plan should be a short Sprint 1 task checklist tied to concrete files/classes/migrations.

Do not design later epics in more implementation detail until Sprint 1 domain behavior is working.

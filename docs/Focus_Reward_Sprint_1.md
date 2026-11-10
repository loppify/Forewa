# Focus Reward — Sprint 1
## Foundation → Identity → Path → Skill Tree → Goal Lifecycle

**Sprint version:** 0.1  
**Target:** first implementation sprint  
**Source:** Domain/Solution Spec v0.2 + Implementation Plan v0.1  
**Primary learning target:** Spring Boot domain implementation with PostgreSQL, JPA, validation, security boundaries, migrations, services, and integration tests.

---

# 1. Sprint Outcome

At the end of this sprint, a real authenticated user must be able to:

1. sign in with Google;
2. have a local `UserAccount`;
3. confirm/edit their timezone;
4. create a personal path;
5. create category and trainable skill nodes;
6. build a valid nested skill hierarchy;
7. create multiple `PLANNED` goals for one trainable skill;
8. have at most one `ACTIVE` goal;
9. activate a planned goal;
10. close an active goal as:
   - `ACHIEVED`
   - `REPLACED`
   - `ABANDONED`
11. retain full goal history after relogin;
12. be unable to access another user's data.

No focus timer, XP, rewards, mastery, quests, maintenance, templates, or AI in this sprint.

---

# 2. Sprint Rule

This sprint is **checkpoint-based, not hour-based**.

Do not stretch work to fill a day.

If a day's checkpoint is finished early and tests pass:

> immediately continue to the next checkpoint.

The sprint ends when the exit test passes, not when seven calendar days pass.

---

# 3. Definition of Done

A feature counts as complete only when:

- migration exists;
- JPA model exists;
- repository exists;
- domain/application service enforces rules;
- request/response DTOs exist;
- validation exists;
- controller/API works;
- ownership is checked server-side;
- important invalid states return explicit errors;
- integration tests use PostgreSQL/Testcontainers;
- data survives application restart/relogin.

Do not count "entity created" as feature completion.

---

# 4. Tech Stack for Sprint 1

Backend:

- Java 21+
- Spring Boot
- Gradle Wrapper
- Spring Web
- Spring Security
- OAuth2 Client / OpenID Connect
- Spring Data JPA
- PostgreSQL
- Flyway
- Jakarta Validation
- Testcontainers PostgreSQL
- JUnit 5

Recommended supporting dependencies:

- Spring Boot Actuator
- PostgreSQL JDBC driver

Do not add:

- Redis
- Kafka
- RabbitMQ
- JWT
- WebFlux
- MapStruct unless genuinely needed
- Lombok unless you already consciously want it
- AI SDKs
- Dockerized application deployment
- Kubernetes

---

# 5. Package Skeleton

Use feature/domain grouping rather than one giant layer package.

Suggested starting structure:

```text
src/main/java/.../
├── auth/
├── user/
├── path/
├── skill/
│   └── goal/
└── common/
    ├── error/
    └── persistence/
```

Do not create packages for future systems yet.

---

# 6. Checkpoint 0 — Bootstrap

## Goal

Spring application boots against PostgreSQL and migrations are authoritative.

## Tasks

### Project

Create Spring Boot project with required dependencies.

### PostgreSQL

Create local Docker Compose containing only PostgreSQL if that is the simplest local setup.

Environment variables:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
GOOGLE_CLIENT_ID
GOOGLE_CLIENT_SECRET
```

Do not hardcode secrets.

### JPA

Production/local schema mode:

```text
ddl-auto = validate
```

Flyway owns schema creation.

### Testcontainers

Create PostgreSQL integration-test infrastructure.

First integration test:

```text
Spring context starts
Flyway migrations apply
database connection succeeds
```

### Error contract

Set up global exception handling using Spring `ProblemDetail`.

Minimum fields:

```text
status
title
code
detail
```

## Done when

```text
./gradlew test
```

starts a real PostgreSQL Testcontainer, applies migrations, and passes.

---

# 7. Checkpoint 1 — UserAccount + Google Authentication

## Goal

Google identity becomes a local isolated user.

## Migration

Create:

```text
V001__user_account.sql
```

Required fields:

```text
id UUID PK
google_subject VARCHAR UNIQUE NOT NULL
email VARCHAR NOT NULL
email_verified BOOLEAN NOT NULL
display_name VARCHAR NULL
avatar_url VARCHAR NULL
timezone VARCHAR NULL
role VARCHAR NOT NULL
created_at TIMESTAMPTZ NOT NULL
updated_at TIMESTAMPTZ NOT NULL
last_login_at TIMESTAMPTZ NOT NULL
deleted_at TIMESTAMPTZ NULL
version BIGINT NOT NULL
```

Initial roles:

```text
USER
ADMIN
```

## Model

Create:

```text
UserAccount
Role
```

Google `sub` is the external identity key.

Do **not** use email as external identity.

## Google login

Configure Spring Security OAuth2/OIDC.

On first successful login:

```text
Google identity
→ create local UserAccount
```

On repeat login:

```text
google_subject
→ load same UserAccount
→ update allowed profile fields
→ update last_login_at
```

Do not store Google access/refresh tokens.

## Current user API

Implement:

```text
GET /api/v1/me
PATCH /api/v1/me
```

`PATCH /me` supports timezone.

Timezone must be a valid IANA `ZoneId`.

## Test strategy

Integration/API tests do not need real Google.

Use Spring Security test support / mocked authenticated principal while testing application authorization.

## Tests

Must cover:

- account created once;
- repeat login does not duplicate;
- same Google subject with changed email remains same user;
- invalid timezone → validation error;
- unauthenticated `/me` rejected.

## Done when

You can log in locally through Google and `/me` returns your persisted local account.

---

# 8. Checkpoint 2 — PersonalPath

## Goal

Authenticated users can create isolated personal progression paths.

## Migration

Create:

```text
V002__personal_path_and_skill.sql
```

First implement `personal_path`.

Fields:

```text
id
user_id FK
name
description
source_template_id NULL
source_template_revision NULL
archived_at NULL
created_at
updated_at
version
```

## API

```text
POST  /api/v1/paths
GET   /api/v1/paths
GET   /api/v1/paths/{pathId}
PATCH /api/v1/paths/{pathId}
POST  /api/v1/paths/{pathId}/archive
```

## Request

Create:

```text
name: required, 1..120
description: optional
```

## Ownership

Every repository/service operation must scope or validate against authenticated user.

Do not do:

```text
findById(pathId)
→ return
```

without ownership verification.

## Tests

- create path;
- list own paths;
- update own path;
- archive own path;
- user A cannot read/update user B path;
- archived path not returned in default active list unless requested.

## Done when

Two users can create identically named paths and only see their own.

---

# 9. Checkpoint 3 — Skill Tree

## Goal

A path can contain a valid hierarchy of categories and trainable leaves.

## Migration

Continue/create in:

```text
V002__personal_path_and_skill.sql
```

or create a separate migration if you prefer smaller migration steps.

Create:

```text
skill_node
```

Fields:

```text
id
path_id FK
parent_id FK NULL
node_type
name
description
sort_order
reward_eligible
mastery_enabled
current_mastery_level
source_template_node_key NULL
archived_at NULL
created_at
updated_at
version
```

For Sprint 1:

```text
reward_eligible
mastery_enabled
current_mastery_level
```

may exist with safe defaults but are not functionally used yet.

Enums:

```text
CATEGORY
TRAINABLE
```

## Rules

### CATEGORY

May have children.

Cannot be directly trainable.

### TRAINABLE

Must remain a leaf.

Cannot have children.

## APIs

```text
POST  /api/v1/paths/{pathId}/skills
GET   /api/v1/paths/{pathId}/skills
GET   /api/v1/skills/{skillId}
PATCH /api/v1/skills/{skillId}
POST  /api/v1/skills/{skillId}/archive
```

## Create request

```text
name
description?
nodeType
parentId?
sortOrder?
```

## Required domain service

Create something equivalent to:

```text
SkillTreeService
```

Responsibilities:

- validate parent belongs to same path;
- validate ownership;
- prevent CATEGORY/TRAINABLE rule violations;
- prevent hierarchy cycles;
- archive safely.

Do not put hierarchy logic in controller.

## Node-type conversion

Sprint-safe rule:

Before progression history exists, conversion may be supported.

Simpler acceptable Sprint 1 rule:

> node type is immutable after creation.

If you choose the simpler rule, document it as a temporary implementation restriction and revisit before beta.

## Tests

Must cover:

- create root CATEGORY;
- create TRAINABLE child;
- create nested CATEGORY;
- cannot add child under TRAINABLE;
- cannot move node under itself;
- cannot create indirect cycle;
- cannot use parent from another path;
- cannot use another user's skill/path;
- path tree persists after restart/reload.

## Done when

You can create:

```text
Software Engineering
└── Backend
    ├── Java
    └── Spring Boot
```

with `Backend` as CATEGORY and Java/Spring as TRAINABLE.

---

# 10. Checkpoint 4 — Goal Lifecycle

## Goal

A trainable skill supports future planned goals plus exactly one current active goal.

## Migration

Create:

```text
V003__skill_goal.sql
```

Fields:

```text
id UUID PK
skill_id UUID FK
title VARCHAR(160)
success_criterion TEXT
baseline_value NUMERIC NULL
target_value NUMERIC NULL
unit VARCHAR(40) NULL
target_date DATE NULL
status VARCHAR
outcome_note TEXT NULL
started_at TIMESTAMPTZ NULL
closed_at TIMESTAMPTZ NULL
created_at TIMESTAMPTZ
updated_at TIMESTAMPTZ
version BIGINT
```

Enum:

```text
PLANNED
ACTIVE
ACHIEVED
REPLACED
ABANDONED
```

## Database invariant

Create PostgreSQL partial unique index:

```text
one ACTIVE goal per skill
```

Conceptually:

```sql
CREATE UNIQUE INDEX ...
ON skill_goal(skill_id)
WHERE status = 'ACTIVE';
```

Do not enforce "one active goal" only in Java.

## Rules

A goal belongs only to a TRAINABLE skill.

Multiple PLANNED:

```text
allowed
```

Maximum ACTIVE:

```text
1
```

Closed states:

```text
terminal
```

## Valid transitions

```text
PLANNED → ACTIVE
PLANNED → ABANDONED

ACTIVE → ACHIEVED
ACTIVE → REPLACED
ACTIVE → ABANDONED
```

Invalid:

```text
ACHIEVED → ACTIVE
ABANDONED → ACTIVE
REPLACED → ACTIVE
```

If user wants that idea again, create a new goal record.

## Create API

```text
POST /api/v1/skills/{skillId}/goals
```

Request includes:

```text
title
successCriterion
baselineValue?
targetValue?
unit?
targetDate?
status = PLANNED | ACTIVE
```

No other creation statuses allowed.

## Read

```text
GET /api/v1/skills/{skillId}/goals
```

Return grouped or sortable:

```text
active
planned
history
```

## Transitions

```text
POST /api/v1/goals/{goalId}/activate
POST /api/v1/goals/{goalId}/achieve
POST /api/v1/goals/{goalId}/replace
POST /api/v1/goals/{goalId}/abandon
```

## Editing

```text
PATCH /api/v1/goals/{goalId}
```

Editable content:

- title;
- criterion;
- baseline;
- target;
- unit;
- target date.

Do not allow generic PATCH to arbitrarily mutate lifecycle status.

Lifecycle uses explicit commands.

## Domain service

Create:

```text
GoalService
```

Own:

- creation;
- transition validation;
- ownership;
- one-active rule;
- timestamps.

## Timestamp semantics

### PLANNED

```text
started_at = NULL
closed_at = NULL
```

### ACTIVE

On activation:

```text
started_at = now
```

### closed

On close:

```text
closed_at = now
```

## Tests

### Planned

- create 1 PLANNED;
- create 5 PLANNED;
- all coexist.

### Active

- create ACTIVE when none exists;
- activate planned when none active;
- second ACTIVE rejected;
- concurrent activation of two planned goals → only one succeeds.

### Transitions

- PLANNED → ACTIVE;
- PLANNED → ABANDONED;
- ACTIVE → ACHIEVED;
- ACTIVE → REPLACED;
- ACTIVE → ABANDONED;
- terminal → anything rejected.

### Ownership/type

- CATEGORY cannot receive goal;
- user B cannot mutate user A goal.

### History

Closed goals remain queryable.

## Done when

This scenario works:

```text
Skill: Pull-ups

ACTIVE
- 10 strict pull-ups

PLANNED
- 15 strict pull-ups
- +20 kg weighted pull-up
```

Then:

```text
10 strict pull-ups → ACHIEVED
15 strict pull-ups → ACTIVE
```

without deleting history.

---

# 11. Checkpoint 5 — API Consistency and Domain Errors

## Goal

The Sprint 1 API behaves like one system, not a collection of controllers.

## Required domain errors

At minimum:

```text
AUTH_REQUIRED
FORBIDDEN
RESOURCE_NOT_FOUND

VALIDATION_FAILED
INVALID_TIMEZONE

SKILL_NOT_TRAINABLE
INVALID_SKILL_HIERARCHY
SKILL_HIERARCHY_CYCLE

ACTIVE_GOAL_ALREADY_EXISTS
INVALID_GOAL_TRANSITION
```

## HTTP mapping

Use consistently:

```text
400 validation/malformed
401 unauthenticated
403 unauthorized
404 resource missing
409 domain-state conflict
500 unexpected
```

Examples:

Second active goal:

```text
409 ACTIVE_GOAL_ALREADY_EXISTS
```

Trying to activate ACHIEVED goal:

```text
409 INVALID_GOAL_TRANSITION
```

Goal on CATEGORY:

```text
409 SKILL_NOT_TRAINABLE
```

## Done when

Controller code does not manually invent one-off response structures.

---

# 12. Checkpoint 6 — Minimal User Interface

## Goal

Complete Sprint 1 through the actual product, not Postman only.

Do not overdesign.

Required UI:

## Login

```text
Sign in with Google
```

## Paths

- list;
- create;
- open.

## Path detail

Nested representation sufficient:

```text
Backend
├── Java
└── Spring Boot
```

Actions:

- add CATEGORY;
- add TRAINABLE.

No graph/constellation yet.

## Skill detail

Show:

```text
skill name
description

Current Goal
Planned Goals
Goal History
```

Actions:

- create planned goal;
- create active goal if none exists;
- activate planned;
- achieve;
- replace;
- abandon.

## UX requirement

The difference between:

```text
CURRENT
PLANNED
HISTORY
```

must be visually obvious.

No anime polish required yet.

## Done when

You can complete the full sprint exit test without Postman or database tools.

---

# 13. Checkpoint 7 — Cleanup and Review

Do not add new functionality.

## Review

Inspect:

- entity boundaries;
- DTO/entity separation;
- transaction placement;
- ownership checks;
- duplicated service logic;
- lazy-loading surprises;
- N+1 queries on tree/goal view;
- migrations;
- index names;
- error consistency;
- test readability.

## Spring concepts you should be able to explain after the sprint

Without memorized textbook definitions, explain from your own implementation:

- why DTO != entity;
- why controller does not own domain transitions;
- why repository does not contain business policy;
- `@ManyToOne`;
- self-referencing relationship;
- transaction boundary;
- optimistic locking;
- partial unique DB index;
- service-level validation vs DB constraint;
- authentication vs authorization;
- why Google `sub` is used instead of email;
- why explicit lifecycle endpoints are preferable to arbitrary status PATCH;
- why one rule is enforced both in Java and PostgreSQL.

If you cannot explain one, inspect your own code until you can.

---

# 14. Sprint Exit Test

Run this only when you think Sprint 1 is complete.

Do not alter the application specifically for the test.

## Setup

Use normal UI.

### 1. Login

Sign in.

Relog once.

Verify same local account.

### 2. Create path

Create:

```text
Calisthenics
```

### 3. Create hierarchy

```text
Upper Body
└── Pull-ups
```

Types:

```text
Upper Body = CATEGORY
Pull-ups = TRAINABLE
```

### 4. Invalid hierarchy

Try to add child under `Pull-ups`.

Expected:

```text
rejected
```

### 5. Goals

Create:

```text
ACTIVE
10 strict pull-ups
```

Criterion:

```text
Perform 10 strict dead-hang pull-ups in one set.
```

Create PLANNED:

```text
15 strict pull-ups
```

Create another PLANNED:

```text
Weighted pull-up +20 kg
```

### 6. Active uniqueness

Try to activate:

```text
15 strict pull-ups
```

while `10 strict pull-ups` is ACTIVE.

Expected:

```text
409 conflict
```

### 7. Goal completion

Mark:

```text
10 strict pull-ups → ACHIEVED
```

Activate:

```text
15 strict pull-ups
```

Expected:

```text
ACTIVE
```

History still contains first goal.

### 8. Persistence

Sign out.

Restart app if practical.

Sign in.

Expected:

- path exists;
- hierarchy exists;
- active goal exists;
- planned goal exists;
- achieved history exists.

### 9. Ownership

Using second test user:

- path should not be visible;
- direct URL/API ID access must fail.

## Sprint PASS

Sprint passes only if all nine sections pass without:

- manual SQL;
- manually changing DB rows;
- disabling security;
- changing code during the test.

---

# 15. What Is Explicitly Forbidden in Sprint 1

Do not start:

- focus stopwatch;
- XP;
- level formula;
- reward money;
- mastery;
- prerequisites;
- quests;
- maintenance;
- templates;
- structured import;
- export;
- AI;
- anime UI;
- achievements;
- cosmetics;
- notifications.

If Sprint 1 finishes early:

> start Sprint 2.

Do not inflate Sprint 1 with random future features.

---

# 16. Suggested Commit Boundaries

Not mandatory, but useful:

```text
chore: bootstrap spring postgres flyway testcontainers

feat(auth): add google oidc and local user account

feat(path): add personal path ownership

feat(skill): add category and trainable skill tree

feat(goal): add planned and active goal lifecycle

feat(api): standardize domain problem responses

feat(ui): add path skill and goal workflows

test: add sprint 1 integration and ownership coverage
```

Prefer coherent commits over one commit per file.

---

# 17. AI Usage During This Sprint

Because this sprint is also Spring learning:

1. first attempt domain/service implementation yourself;
2. use AI to explain framework behavior or review your code;
3. use hints when stuck;
4. after your implementation exists, AI may propose refactors/tests;
5. do not blindly paste a complete generated feature that you cannot explain.

The exit criterion is not only "it works".

You should understand why the Spring code works.

---

# 18. Immediate First Task

Start only with Checkpoint 0.

Create the Spring project and get this passing:

```text
Spring context
+
Flyway
+
real PostgreSQL Testcontainer
=
green test
```

Do not create `UserAccount` until that foundation is clean.

After that, move directly to Checkpoint 1.

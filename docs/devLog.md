# Development Log — Focus Reward

This log records the developer's work and learning for career reflection. It distinguishes implemented work from verified behavior and open follow-ups. Existing code or tests are not attributed to the developer unless their authorship is known.

## Learning and implementation history

### Path ownership and API testing — implemented; broader suite needs cleanup

- Implemented user-scoped path detail lookups using both the requested path ID and the authenticated user's ID.
- Wrote MockMvc tests for path creation, listing, updating, archiving, and user isolation.
- Practiced the full HTTP test flow: OIDC-authenticated test requests, CSRF tokens on state-changing requests, response assertions, and extracting JSON responses.
- Learned to verify persisted ownership by following the resource-to-owner relationship rather than comparing unrelated IDs.
- Existing `Checkpoint1Test` was not authored by the developer; it is not claimed as their work.

### Skill-tree API and domain rules — in progress

- Implemented the `SkillNode` persistence model, category/trainable types, request/response DTOs, repository queries, and controller/service routes for creating, listing, reading, modifying, and archiving skills.
- Added owner-scoped path/skill lookups and parent validation for same-path membership and the `CATEGORY`/`TRAINABLE` rule.
- Learned how Spring Data derived-query names traverse nested entity properties, including the skill → path → user ownership chain.
- Added optional `sortOrder` handling and learned that the service must choose an order when the request omits it; the current defaulting behavior still merits review for gaps and root-node ordering.
- Changed the response parent field to a nullable parent UUID rather than serializing a `SkillNode` entity, avoiding nested persistence data and account-field exposure.

### Hierarchy-cycle validation — targeted test verified

- Implemented an ancestor-walk check when reparenting a skill: start at the proposed parent and follow parent links toward the root; reject the move if the moved node is encountered.
- Added a test that persists a valid three-node chain and attempts to move its root under a descendant.
- Verified the indirect-cycle scenario in the focused test and again in the full clean suite.
- Learned that a cycle test should build a valid persisted tree and exercise a move that would create a cycle, rather than relying on unsaved circular JPA objects.

### Error handling and optional PATCH fields — concepts reviewed

- Learned the checked/unchecked exception distinction and why application domain exceptions commonly extend `RuntimeException` and are translated centrally by `@RestControllerAdvice`.
- Reviewed how to choose API outcomes for validation errors, missing resources, authorization failures, and state conflicts using `ProblemDetail`.
- Worked through PATCH semantics: omitted fields should remain unchanged; request DTOs need nullable fields for optional input, with validation applied only to supplied values. This remains a follow-up for the path PATCH endpoint, which still uses the create request type.

### Mockito agent and test-runner diagnostics — in progress

- Added Mockito as a Java agent for Maven Surefire after encountering JDK dynamic-agent warnings.
- POM review found Surefire's `@{argLine}` placeholder had no project property; the developer added an empty `<argLine/>` property, allowing the standard test JVM to start.
- Test output shows Maven is running on JDK 27 even though `java.version` targets Java 21. Align the Maven runtime with JDK 21 (or deliberately configure a supported toolchain) when addressing JDK-specific warnings.

### Goal creation slice — in progress — 2026-11-16

- Added the initial `SkillGoal` entity, create request/response DTOs, repository, service/controller endpoint, and `V3__skill_goal.sql` migration.
- Learned to compare JPA entity mappings and Flyway column definitions using Hibernate's `ddl-auto=validate` rather than letting Hibernate alter the schema.
- Corrected the numeric and date migration types to match the `BigDecimal` and `LocalDate` entity mappings, and retained the partial unique index for one active goal per skill rather than an unconditional unique constraint.
- Updated goal creation to associate an owner-scoped skill, reject category skills, permit planned goals alongside an active goal, initialize the active start time, validate enum presence, and return a conflict for an existing active goal.
- Added goal listing, activation, achievement, replacement, abandonment, and content-modification operations. Updated transitions to reject reactivating closed goals, made replacement close the active goal, and made PATCH text constraints permit omitted values while rejecting blank supplied text.
- Owner-scoped goal listing and specific invalid-transition handling are now implemented. Error-contract follow-ups: route category-goal rejection through the specific not-trainable exception (the handler currently maps that code/status differently from the Sprint contract), map attempts to abandon an already closed goal to the transition error, add validation-error handling, and add goal endpoint tests. Checkpoint 4 remains in progress; no tests were run during the latest review, as requested.
- Encountered a concurrent active-goal insert/update rejected by PostgreSQL's `one_active_goal_per_skill_constraint`. This confirms the database invariant is acting as the final concurrency guard; translate only this named constraint violation to the existing `ACTIVE_GOAL_ALREADY_EXISTS` 409 response. Follow-up remains open.

## Verification history — 2026-11-15

- An earlier non-clean test run discovered a stale compiled `Checkpoint2Test` after its source was renamed. Its old cleanup code caused foreign-key errors; the current `Checkpoint2And3Test.clean()` deletes skills before paths.
- Latest `mvn clean test`: **21 tests run, 0 failures, 0 errors**. The clean build removed stale test bytecode left by renaming the test class, and null-safe parent-ID response mapping fixed the root-skill NPE.
- Checkpoint 3 is **in progress**, not complete. Remaining test coverage includes GET/list and individual skill reads, ordinary PATCH behavior, archive persistence, self-parent rejection, and tree persistence after reload/restart.

## Current verification — 2026-11-16

- Latest `mvn test` — 2026-11-16: **21 tests run, 0 failures, 0 errors** after the goal-model/migration updates.
- There are still no goal-specific tests in the test suite, so the goal creation endpoint and its domain rules are not yet verified. The new goal-creation slice remains **in progress**.

## Skills practiced

- Java records, nullable wrapper types, UUID value equality, and exception classes.
- Spring MVC controllers, MockMvc, Spring Security OIDC test principals, and CSRF.
- Spring services/repositories, JPA self-referencing relationships, entity ownership, and transactions.
- PostgreSQL foreign keys, Flyway migrations, Testcontainers, Maven Surefire, and Mockito's Java-agent setup.
- Domain validation, privacy-aware ownership checks, and translating domain exceptions to HTTP `ProblemDetail` responses.

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
- Owner-scoped goal listing and specific invalid-transition handling are now implemented. Error-contract follow-ups: route category-goal rejection through the specific not-trainable exception, map unexpected validation failures consistently, and avoid exposing raw database details in the active-goal constraint response. Checkpoint 4 remains in progress.
- Added `Checkpoint4Test` coverage for multiple planned goals, active-goal creation/activation, sequential conflict, and concurrent activation. Latest review found missing assertions and lifecycle/ownership test cases; no tests were run during that review.
- Encountered a concurrent active-goal insert/update rejected by PostgreSQL's `one_active_goal_per_skill_constraint`. This confirms the database invariant is acting as the final concurrency guard; translate only this named constraint violation to the existing `ACTIVE_GOAL_ALREADY_EXISTS` 409 response. Follow-up remains open.

## Verification history — 2026-11-15

- An earlier non-clean test run discovered a stale compiled `Checkpoint2Test` after its source was renamed. Its old cleanup code caused foreign-key errors; the current `Checkpoint2And3Test.clean()` deletes skills before paths.
- Latest `mvn clean test`: **21 tests run, 0 failures, 0 errors**. The clean build removed stale test bytecode left by renaming the test class, and null-safe parent-ID response mapping fixed the root-skill NPE.
- Checkpoint 3 is **in progress**, not complete. Remaining test coverage includes GET/list and individual skill reads, ordinary PATCH behavior, archive persistence, self-parent rejection, and tree persistence after reload/restart.

## Current verification — 2026-11-16

- Latest `mvn test` — 2026-11-16: **21 tests run, 0 failures, 0 errors** after the goal-model/migration updates.
- There are still no goal-specific tests in the test suite, so the goal creation endpoint and its domain rules are not yet verified. The new goal-creation slice remains **in progress**.

## Latest test diagnosis — 2026-11-19

- The developer reported `Checkpoint4Test.testTransitionsViolation` failing on a second direct repository save with `ObjectOptimisticLockingFailureException`.
- Diagnosis: `SkillGoal` has `@Version`; the first save increments the persisted version. The test then reuses an older detached instance instead of the version returned by `save` or reloaded from the repository. The next save is rejected as stale.
- Updated the test to keep the entity returned from each save when manually seeding terminal statuses, so the next update carries the latest `@Version`. This avoids reusing the stale detached instance; rerun is still needed to verify.
- Test-design follow-up: prefer creating terminal states through lifecycle operations, or seed matching lifecycle timestamps when direct persistence setup is intentional.
- The developer chose to defer these test refinements and stronger concurrent-conflict assertions until the end of the sprint. Keep them as deferred follow-ups; focus on the current checkpoint work first.

## Error-contract review — 2026-11-20

- Reviewed `GlobalExceptionHandler` against Sprint 1 Checkpoint 5 without running tests.
- Existing handlers provide `ProblemDetail` responses for several path, skill, and goal domain errors. Follow-ups: align `SKILL_IS_NOT_TRAINABLE` and `NodeTypeViolation` mappings with the documented codes/statuses; add a stable validation-error response; keep database constraint details out of client responses; and configure authentication/authorization failures in Spring Security's filter chain.
- The Sprint document requires `RESOURCE_NOT_FOUND`, while current resource handlers emit resource-specific codes. Confirm whether those specific codes are acceptable additions or whether the contract should use the generic code.

## API security errors — in progress — 2026-11-22

- Added `ApiAuthenticationEntryPoint` and `ApiAccessDeniedHandler` in `SecurityConfig`, returning ProblemDetail bodies with `AUTH_REQUIRED` (401) and `FORBIDDEN` (403). These are user-authored additions following assistant examples; no successful request-level verification is recorded yet.
- 2026-11-23: Wired an ordered `/api/**` security filter chain to both handlers, leaving the existing Google OIDC chain for other requests. An early focused run confirmed API 401/`AUTH_REQUIRED` while an older test still expected a redirect; that test was later updated. The developer reports real Google login works. CSRF response-body coverage remains a separate follow-up.

## Sprint 1 Checkpoint 6 — UI implementation by assistant — 2026-11-23

- At the developer's explicit request, the assistant built a desktop-first, phone-usable Spring-served HTML/CSS/JavaScript UI. The visual language is modern and anime-inspired through panels, color, and subtle geometry, without characters or character artwork. These UI files are assistant-authored, not claimed as the developer's own coding work.
- The interface covers Google sign-in, account timezone confirmation/editing and sign-out, path creation/list/detail, nested category/trainable skill trees, and current/planned/historical goals with creation, editing, activation, achievement, replacement, and abandonment.
- The assistant allowed public access to the landing page and static assets in the browser security chain, kept the API chain authenticated, and disabled API saved requests so anonymous page initialization does not redirect login back to JSON.
- Verification: `node --check src/main/resources/static/app.js` passed; `mvn -Dtest=FrontendAccessTest test` passed (2 tests), confirming public UI assets and API 401/403 ProblemDetail responses. Early assertion/contract test failures were corrected. On 2026-11-23 the developer reports completing a manual visual UI walkthrough and confirming real Google login works; this is user-reported verification, not assistant-run browser testing.

## Alternate skill graph — assistant implementation — 2026-11-23

- At the developer's request, the assistant retained the nested skill tree and added an optional Obsidian-like graph view with connected category/trainable nodes. The Graph view switch lives in the existing account/settings panel and is stored in browser localStorage separately for each account; no backend preference or domain state changes were made.
- The graph uses canvas to draw hierarchy links and labels and supports click-to-open, node dragging, background panning, wheel/button zoom, fit-to-view, responsive sizing, and a keyboard-accessible skill list. Moving a visual node does not change its saved parent relationship.
- Verification: `node --test src/test/js/skill-graph.test.cjs` passed (node selection, drag without navigation, cleanup), JavaScript syntax checks passed for `app.js` and `skill-graph.js`, and `mvn -Dtest=FrontendAccessTest test` passed (2 tests, including public access to the graph script). The developer reports the manual graph/UI visual walkthrough passed on 2026-11-23.

## Current checkpoint verification — 2026-11-23

- The developer reports Google login and the manual UI/graph visual walkthrough work. The assistant independently ran `mvn clean test`: **31 tests, 0 failures, 0 errors, 0 skipped**. This verifies the automated suite, not the full nine-part manual Sprint 1 exit test.
- Next gate: confirm the full Sprint 1 exit scenario in the normal UI, especially blocked child-under-trainable behavior, goal history after relogin/restart, and direct cross-user ID access. Then do Checkpoint 7 cleanup/review; do not mark the sprint complete solely from a green build or visual walkthrough.
- Follow-up report from the developer: the relogin/persistence and second-user ownership checks asked about in review are good. This is user-reported manual verification; the remaining Checkpoint 7 code cleanup/review has not yet been performed.

## Checkpoint 7 review — 2026-11-23

- Developer reported the preceding UI and ownership/persistence walkthroughs working and made a small goal-test expectation update to align category-goal errors with 409. Assistant review inspected Sprint 1 entity/service/security/error boundaries against the newer BRD v0.4 and Domain Spec v0.3 (which leave Sprint 1 scope unchanged).
- `mvn clean test` independently verified again: **31 tests, 0 failures, 0 errors, 0 skipped**. This does not cover all archive/patch edge cases.
- Review findings at that point: goal PATCH permitted changing closed-goal content (historical criterion integrity); skill PATCH permitted blank names; archiving a category with active descendants could detach them from the visible tree; request-validation errors used `INVALID_REQUEST` rather than the documented `VALIDATION_FAILED` code. These were review guidance, not completed fixes at the time.

## Checkpoint 7 follow-up — 2026-11-23

- The developer changed skill PATCH validation to allow an omitted name while rejecting supplied blank names, changed the skill archive API route to `/api/v1/skills/{skillId}/archive`, and mapped bean-validation failures to `VALIDATION_FAILED`. The developer chose to reject archiving categories with active children; this archive rule is a design decision, not implemented yet.
- Independent `mvn clean test`: **31 tests, 0 failures, 0 errors, 0 skipped**. These tests do not verify category-archive rejection or the frontend archive action.
- Follow-ups: implement the category-archive guard and its HTTP/domain error, update the assistant-authored frontend archive request to the renamed API route (currently still posts to `/api/v1/skills/{id}`), and verify both. Historical goal editing remains an unresolved policy question from review. Checkpoint 7 remains in progress.

## Skill archive consistency — assistant fix — 2026-11-23

- At the developer's explicit request, the assistant added a guard to reject archiving a category with unarchived direct children, returning `409 INVALID_SKILL_HIERARCHY` with an actionable message. A leaf can still be archived, and a category can be archived after its children are archived. This avoids leaving active children orphaned in the tree.
- The assistant updated the UI archive action to call the documented `/api/v1/skills/{skillId}/archive` endpoint and added a PostgreSQL-backed MockMvc test of both rejection and the successful child-then-parent archive sequence. These changes are assistant-authored.
- Verification: `mvn -Dtest=SkillArchiveTest test` passed (1 test), `node --check src/main/resources/static/app.js` passed, and `git diff --check` passed. The full suite has not been rerun since these changes. Historical goal-editing policy from Checkpoint 7 remains an open review question.

## Post-archive verification — 2026-11-24

- Assistant ran `mvn clean test` after the archive guard, UI route fix, and integration test: **32 tests, 0 failures, 0 errors, 0 skipped**. The prior note about not having rerun the full suite is now superseded.
- Category-archive behavior is covered by a focused PostgreSQL-backed test, but the updated frontend archive button has not been manually rechecked since the URL change. Selected Sprint 1 manual checks are user-reported; completion of the exact nine-part exit test has not been explicitly confirmed. Historical closed-goal editing remains a product-policy decision before declaring Checkpoint 7 fully resolved.

## Closed-goal history decision — 2026-11-24

- The developer decided that `ACHIEVED`, `REPLACED`, and `ABANDONED` goals are read-only. This preserves the recorded success criterion and other historical content after a goal closes.
- Implementation remains open: reject PATCH on closed goals in the goal service before applying fields, return the existing invalid-goal-transition conflict response, and verify that rejected edits leave the saved goal unchanged. `PLANNED` and `ACTIVE` goals remain editable.

## Closed-goal read-only implementation — assistant fix — 2026-11-24

- Following the developer's decision and request to carry the work forward, the assistant added a pre-mutation guard in `GoalService.modifySkillGoal`. PATCH now rejects `ACHIEVED`, `REPLACED`, and `ABANDONED` goals with `409 INVALID_GOAL_TRANSITION`; `PLANNED` and `ACTIVE` remain editable. This implementation and its test are assistant-authored.
- Added a PostgreSQL-backed MockMvc test covering both editable states and all three closed states, including checks that rejected updates leave title, criterion, and status unchanged.
- Verification: focused test passed (1 test); subsequent `mvn clean test` passed **33 tests, 0 failures, 0 errors, 0 skipped**. The previous note that implementation remained open is superseded.

## Skills practiced

- Java records, nullable wrapper types, UUID value equality, and exception classes.
- Spring MVC controllers, MockMvc, Spring Security OIDC test principals, and CSRF.
- Spring services/repositories, JPA self-referencing relationships, entity ownership, and transactions.
- PostgreSQL foreign keys, Flyway migrations, Testcontainers, Maven Surefire, and Mockito's Java-agent setup.
- Domain validation, privacy-aware ownership checks, and translating domain exceptions to HTTP `ProblemDetail` responses.

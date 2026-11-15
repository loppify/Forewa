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
- Current API response DTO includes a `SkillNode` parent entity. Review identified that serializing this entity exposes nested persistence data, including account fields; replace it with an intentional response representation before considering the API complete.

### Hierarchy-cycle validation — targeted test verified

- Implemented an ancestor-walk check when reparenting a skill: start at the proposed parent and follow parent links toward the root; reject the move if the moved node is encountered.
- Added a test that persists a valid three-node chain and attempts to move its root under a descendant.
- Verified with `mvn -DforkCount=0 -Dtest=Checkpoint2Test#cannotCreateIndirectCycle test`: **1 test passed**.
- Learned that a cycle test should build a valid persisted tree and exercise a move that would create a cycle, rather than relying on unsaved circular JPA objects.

### Error handling and optional PATCH fields — concepts reviewed

- Learned the checked/unchecked exception distinction and why application domain exceptions commonly extend `RuntimeException` and are translated centrally by `@RestControllerAdvice`.
- Reviewed how to choose API outcomes for validation errors, missing resources, authorization failures, and state conflicts using `ProblemDetail`.
- Worked through PATCH semantics: omitted fields should remain unchanged; request DTOs need nullable fields for optional input, with validation applied only to supplied values. This remains a follow-up for the path PATCH endpoint, which still uses the create request type.

### Mockito agent and test-runner diagnostics — in progress

- Added Mockito as a Java agent for Maven Surefire after encountering JDK dynamic-agent warnings.
- A normal `mvn test` currently fails before test execution because Surefire receives the unresolved `@{argLine}` placeholder. Mockito's documented Maven setup requires the late-replaced `argLine` property to exist (an empty property is the documented fallback when no other agent configuration defines it).
- Confirm the POM configuration and rerun the standard test command; do not count the full suite as passing yet.

## Current verification and follow-ups — 2026-11-15

- `mvn test`: **failed before tests ran**; forked JVM received literal `@{argLine}`.
- Diagnostic run `mvn -DforkCount=0 test`: **21 tests run, 1 assertion failure, 8 errors**.
  - `Checkpoint2Test.createRootCategory` expected `"Java"` although the request sends `"BACKEND"`.
  - Eight setup errors originate in `Checkpoint2Test.clean()`: it deletes paths before deleting skill nodes that reference them, violating the foreign key. Delete dependent test data before its referenced rows.
- The focused indirect-cycle test passes, as noted above.
- Checkpoint 3 is **in progress**, not complete. Remaining work includes fixing test setup/assertions, getting the standard Maven test invocation working, testing self-parent rejection and tree persistence after reload/restart, verifying archive persistence, and addressing entity serialization in `SkillNodeResponse`.

## Skills practiced

- Java records, nullable wrapper types, UUID value equality, and exception classes.
- Spring MVC controllers, MockMvc, Spring Security OIDC test principals, and CSRF.
- Spring services/repositories, JPA self-referencing relationships, entity ownership, and transactions.
- PostgreSQL foreign keys, Flyway migrations, Testcontainers, Maven Surefire, and Mockito's Java-agent setup.
- Domain validation, privacy-aware ownership checks, and translating domain exceptions to HTTP `ProblemDetail` responses.

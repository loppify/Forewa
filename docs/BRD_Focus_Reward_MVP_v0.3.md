# Business Requirements Document (BRD)
## Focus Reward — Real-Life Skill RPG MVP

**Document version:** 0.3  
**Status:** Draft for implementation planning  
**Date:** 2026-10-28  
**Product name:** `Focus Reward` (temporary working codename; final brand name is intentionally unresolved)  
**Product owner / initial administrator:** Founder  
**Primary implementation objective:** Build a usable MVP that can test whether competence-centered life gamification creates enough recurring value to justify continued development.

**v0.3 change summary:** ordinary stopwatch with a 10-hour sanity threshold and manual review; no periodic 20-minute interruptions; cumulative €1 per 20 reward-eligible minutes with €5/day cap; exact 100-level exponential curve; measurable goals mandatory, mastery optional; prerequisite conditions may use goal completion or mastery; reward eligibility per skill; timezone rules; versioned JSON export; template revision semantics finalized.


# 1. Executive Summary

Focus Reward is a multi-user web application that treats real-life skill development as a game without reducing progress to completed checkboxes.

The product separates four concepts:

1. **Goal** — the concrete, measurable or otherwise verifiable endpoint for which a skill is being trained.
2. **Practice** — actual focused time invested into the skill.
3. **Mastery** — an optional capability-assessment layer for skills where staged competence is meaningful.
4. **Reward** — a personal reward budget earned from eligible focused work.

A user organizes development into paths and skills, defines a concrete goal for every active trainable skill, records focused practice with a normal stopwatch, earns XP and optional reward credit, completes quests, and—where useful—progresses through explicit mastery criteria. Acquired skills can be maintained through recurring minimum-action routines.

The MVP tests whether this model creates recurring value for real users:

> A concrete endpoint + visible practice + optional demonstrated mastery + dependency-aware skill paths + maintenance routines + immediate rewards will be more useful and motivating for deliberate skill acquisition than a generic task/habit tracker.

The MVP is intended for external beta users, not only founder dogfooding. It must support Google sign-in, isolated private data, reliable session recovery, editable/forkable templates, machine-readable export, account deletion, and a complete usable core loop.

Integrated AI remains outside the first MVP. Manual/admin-authored structured drafts and import provide the future integration seam.

# 2. Document Purpose

This BRD defines:

- the business problem;
- target users and stakeholders;
- MVP hypotheses;
- measurable objectives;
- scope and exclusions;
- business, stakeholder, functional, and non-functional requirements;
- product rules;
- acceptance conditions;
- MVP validation plan;
- release gates;
- risks, assumptions, and decision criteria.

Technical design, database schema, API contracts, frontend component architecture, deployment topology, and detailed test plans belong in later specifications.


# 3. Methodological Basis

This BRD uses a hybrid of current business-analysis and MVP practices:

- separate business, stakeholder, solution, and transition concerns;
- give important requirements an identifier, priority, and verification method;
- define explicit in-scope and out-of-scope boundaries;
- trace requirements to objectives or hypotheses;
- treat the MVP as a product usable by real users with real data, not a screen prototype;
- measure activation, repeated usage, time-to-value, reliability, and qualitative differentiated value;
- test the riskiest assumptions before investing in advanced game systems or AI.


# 4. Product Vision

## 4.1 Vision statement

Create a personal progression system in which a user treats real-life development as a character build while always knowing **what concrete endpoint each active skill is moving toward**.

The system should make it possible to:

- choose a real trainable skill;
- define a measurable personal goal before training it;
- decompose the skill into specific subskills;
- practice with a low-friction persistent focus stopwatch;
- separate accumulated practice XP from actual mastery;
- unlock new branches through capability rather than raw time;
- convert acquired skills into sustainable maintenance routines;
- earn a limited real-life reward budget from focused work;
- export the resulting personal skill model for external analysis, including AI analysis;
- start from editable templates instead of building everything from zero.

## 4.2 Positioning hypothesis

The product is **not primarily a to-do list, generic habit tracker, stopwatch, Pomodoro clone, or virtual RPG**.

Its intended position is:

> A real-life skill progression system for people who want measurable endpoints, deliberate practice, competence checks, dependency-aware skill trees, maintenance routines, and game-like progression.

## 4.3 Core differentiation hypothesis

The proposed differentiation is:

**goal ≠ practice ≠ mastery ≠ reward**

combined with:

**measurable endpoint → deliberate practice → demonstrated mastery → unlocks → maintenance habit**

XP, levels, coins, quests, timers, templates, and skill trees are presentation/mechanism choices, not the differentiated value by themselves.

# 5. Business Problem

People who deliberately learn multiple skills often use disconnected systems:

- task managers for work items;
- timers for focus;
- notes for learning plans;
- spreadsheets or memory for progress;
- habit trackers for repetition;
- separate reward systems;
- no explicit model for whether a skill is actually usable independently.

This creates several problems:

1. Time spent is confused with actual competence.
2. Generic task completion does not represent skill acquisition.
3. Learning plans and daily actions are disconnected.
4. Long-term skill dependencies are difficult to visualize.
5. Previously learned skills may be neglected without a maintenance signal.
6. External rewards are inconsistent.
7. Existing productivity systems can feel administrative rather than engaging.

The proposed product attempts to unify these processes without becoming a full learning-management system.


# 6. Current State and Desired Future State

## 6.1 Current state

A motivated learner may currently:

- have a vague desire such as "get healthier" or "learn German";
- ask an AI, teacher, coach, or course for a learning plan;
- store the plan in notes;
- execute tasks separately;
- use a stopwatch and occasionally forget to stop it;
- confuse hours spent with actual capability;
- lose sight of the endpoint;
- mentally track which skills are becoming rusty;
- reward themselves inconsistently;
- use separate apps for tasks, habits, focus, and notes.

## 6.2 Desired future state

A user can:

1. create or fork a path;
2. choose a specific leaf/trainable skill;
3. define one current concrete measurable/verifiable goal for it;
4. inspect prerequisites and, where enabled, mastery criteria;
5. select/create a quest;
6. start an ordinary stopwatch without periodic forced interruptions;
7. finish normally or correct the recorded duration manually;
8. recover a forgotten session safely if elapsed time becomes implausible;
9. receive deterministic XP for confirmed focused minutes;
10. earn personal reward credit only for reward-eligible focus;
11. attempt mastery checks where the skill uses mastery;
12. unlock dependent skills from goal completion and/or mastery conditions;
13. move an acquired skill into a maintenance routine;
14. export a skill/subtree/full tree for backup or external analysis;
15. edit personal content without affecting source templates.

# 7. Target Users

## 7.1 Primary MVP user

A self-directed learner who:

- actively learns one or more practical skills;
- already uses plans, courses, AI, teachers, projects, or deliberate practice;
- values visible progress;
- is comfortable defining or accepting explicit skill criteria;
- is willing to record focused work;
- wants game-like motivation without turning progress into arbitrary checkbox points.

Examples:

- software developers;
- language learners;
- students;
- creators;
- musicians;
- technically minded self-improvement users.

## 7.2 Explicitly not the initial target

The MVP is not optimized for:

- children;
- enterprise employee performance management;
- formal educational grading;
- medical/therapeutic treatment;
- users wanting only a simple timer;
- users wanting a full fantasy RPG independent of real-world training.


# 8. Stakeholders

| Stakeholder | Interest | MVP responsibility |
|---|---|---|
| Founder / Product Owner | Validate product idea; use product personally | Product decisions, templates, beta recruitment, feedback review |
| Founder / Administrator | Maintain curated content | Create/edit/publish template content |
| MVP User | Improve skills and track progression | Use core loop and provide feedback |
| Developer | Build and operate reliable MVP | Implementation, testing, deployment, monitoring |
| Future content contributor | Create high-quality paths/criteria | Out of MVP |

The same person may hold Founder, Product Owner, Administrator, and Developer roles during MVP.


# 9. MVP Hypotheses

## H1 — Concrete-goal value
Requiring one measurable/verifiable current endpoint for each active trainable skill will make progression directed rather than aimless.

Invalidating evidence: users routinely ignore goals, create meaningless placeholders, or find the requirement obstructive.

## H2 — Practice vs capability value
For skills where staged competence is useful, users will understand and value the distinction between **practice XP** and **mastery**.

Invalidating evidence: mastery is consistently treated as unnecessary administration.

## H3 — Low-friction stopwatch reliability
A normal stopwatch plus a hard sanity threshold/manual review will preserve deep focus better than periodic check-ins while preventing forgotten timers from silently creating absurd progress.

Invalidating evidence: users frequently lose accurate session data or find review/correction too burdensome.

## H4 — Reward motivation
Visible XP plus a bounded personal reward balance will increase willingness to record and repeat eligible focused work.

Invalidating evidence: reward logging is ignored or feels artificial.

## H5 — Skill-path value
A structured path with personal goals and dependency conditions will feel more useful than an unstructured list of tasks/habits.

Invalidating evidence: users rarely revisit the path.

## H6 — Setup friction
Templates/forks will reduce setup friction enough for users to start training quickly.

Invalidating evidence: users abandon onboarding before first focus.

## H7 — Self-assessed mastery is sufficient where enabled
Users can apply explicit mastery criteria honestly enough for the feature to remain useful without external grading.

Invalidating evidence: users arbitrarily self-promote or avoid the feature.

## H8 — Maintenance-routine value
Turning learned skills into small recurring routines will be more actionable than passive freshness warnings.

Invalidating evidence: users ignore the routines or recreate them elsewhere.

# 10. Business Objectives

## OBJ-01 — Validate the core value proposition
Determine whether external users repeatedly use the goal → focus → mastery → maintenance loop.

## OBJ-02 — Validate comprehensibility
A new user should understand Path, Skill, Goal, Quest, Focus Session, XP, Mastery, Reward Balance, and Maintenance Routine without a live founder walkthrough.

## OBJ-03 — Validate time-to-value
A template user should reach:

Google sign-in → fork path → set personal goal → start first focus session

with setup taking about **≤5 minutes**, excluding actual focus time.

## OBJ-04 — Obtain reliable beta evidence without building an analytics product
Use domain records already required by the product plus direct user feedback to evaluate activation, repeated use, mastery interaction, and retention.

A dedicated user-facing analytics dashboard and third-party product-analytics integration are not MVP requirements.

## OBJ-05 — Validate data ownership
Users must be able to export their skill data in a machine-readable form and delete their account/data.

## OBJ-06 — Control MVP scope
A feature enters MVP only if it:
- enables the core journey;
- removes critical friction;
- protects user data/reliability;
- or provides evidence necessary to decide whether to continue.

# 11. Non-Goals

The MVP does not attempt to prove:

- AI can design perfect learning plans;
- the product can replace teachers/courses;
- mastery can be objectively certified;
- social competition improves behavior;
- users will buy cosmetics;
- a complex virtual economy improves retention;
- a native app is necessary;
- every real-world skill can be modeled perfectly.


# 12. MVP Scope — Must-have Capabilities

## A. Accounts and private state

MVP authentication is **Google sign-in**.

The application retains only the Google identity data needed for the product:
- provider subject identifier (`sub`);
- email;
- email verification status where provided;
- display name;
- optional avatar URL.

The application does not need to retain Google OAuth access/refresh tokens after authentication unless a later Google API integration explicitly requires them.

Every user has an editable **IANA timezone**. The browser timezone is proposed on first use. The timezone is used for calendar-day reward caps and display.

## B. Personal paths, skills, goals, and prerequisites

A user can:
- create a blank path;
- fork a published template revision;
- edit the personal copy;
- create nested skills;
- define prerequisite conditions.

Only **leaf/trainable skills** can receive direct focus time/XP.

Each active trainable skill has **exactly one current goal**. A goal must state a measurable or otherwise clearly verifiable endpoint.

Examples:
- "10 strict pull-ups"
- "Hold a 10-minute basic German conversation"
- "Implement Spring authentication without a tutorial"
- "Play piece X at 100 BPM without stopping"

Insufficient goals:
- "be healthy"
- "become smarter"
- "earn $10k"
- "get better at programming"

Goal fields:
- title;
- success criterion;
- optional baseline;
- optional numeric target + unit;
- optional target date;
- status: ACTIVE / ACHIEVED / REPLACED / ABANDONED;
- history.

Completing a goal does not delete the skill. The user may create a new current goal for the same skill.

## C. Optional mastery model

Mastery is **optional per trainable skill** because the universal staged model does not fit every performance skill.

When enabled, the default MVP scale is:
- 0 Unknown
- 1 Guided
- 2 Independent
- 3 Defensible
- 4 Evidence

Criteria for levels 1–4 are user-defined or inherited from templates.

XP never changes mastery automatically.

Prerequisites may use either:
- `GOAL_ACHIEVED`, or
- `MASTERY_AT_LEAST(level)`.

This allows, for example:
- Handstand Walking requires a Handstand goal to be achieved.
- Spring Security requires Spring Boot mastery ≥ Independent.

## D. Focus stopwatch and forgotten-session handling

Focus uses an ordinary uninterrupted stopwatch.

Flow:
1. user selects one active leaf skill and optionally one quest;
2. server records `startedAt`;
3. UI displays elapsed time;
4. user finishes and may correct the actual focused duration;
5. server credits the confirmed duration.

There are **no mandatory periodic 20-minute check-ins**.

### 10-hour sanity threshold

A single session can receive automatic credit only while elapsed time is **≤10 hours**.

If elapsed time exceeds 10 hours:
- the session becomes `REVIEW_REQUIRED`;
- no XP or reward is awarded automatically;
- user enters the actual focused duration or discards the session;
- maximum credited duration for one corrected session is 600 minutes.

This threshold represents loss of trust in the stopwatch, not a claim that a person cannot work longer in a day.

A refresh/browser restart does not lose the session because elapsed time is derived from server timestamps.

A non-blocking "session still running" warning after a long duration may be added later, but notification permission/push infrastructure is **not required for MVP**.

## E. XP and character progression

- **1 confirmed focused minute = 1 XP**.
- XP is credited only to the directly trained leaf skill.
- Parent/category skills may display aggregated descendant XP/time but do not receive direct XP.
- Character XP is total non-duplicated confirmed focus minutes.
- Quests, goal completion, and mastery checks create no bonus XP in MVP.

### Character levels

MVP has **100 visual character levels**.

Let `L` be an integer from 1 to 100.

The total XP threshold is:

`XP(L) = round(600000 × (exp(0.05 × (L - 1)) - 1) / (exp(0.05 × 99) - 1))`

Properties:
- Level 1 = 0 XP.
- Level 100 = 600,000 XP.
- 600,000 XP = 10,000 confirmed focus hours.
- Each next level requires more XP than the previous level.
- Character level is the highest `L` whose threshold is ≤ lifetime XP.
- Lifetime XP/time may continue beyond Level 100; visual level remains 100.

The level curve is cosmetic/meta progression and never proves skill mastery.

## F. Reward ledger

Reward is intentionally separate from XP.

Default rule:
- **€1 reward credit per cumulative 20 reward-eligible focused minutes**
- **maximum €5 earned reward credit per user-local calendar day**

Reward minutes accumulate across eligible sessions within the same local day. Session boundaries do not matter.

Example:
- 12 eligible min + 8 eligible min = €1.
- 45 eligible min total = €2.
- 140 eligible min total = €5 because of the daily cap.

Each trainable skill has a `reward eligible` setting. This allows mental-work skills to earn personal reward while, for example, sport can be excluded if the user prefers.

Manual time corrections recalculate the affected day's earned reward under the same rule. Corrections use ledger adjustments rather than silently inventing a new balance.

Reward balance is personal bookkeeping, not real stored money.

## G. Quests

Users can create/edit/archive/complete quests linked to leaf skills and inherit starter quests from templates.

A quest is a concrete action supporting the current goal and/or next mastery criterion.

Quest completion creates no direct XP or mastery.

## H. Mastery checks

Only skills with mastery enabled expose mastery checks.

User can:
- view next criterion;
- record an attempt;
- PASS/FAIL;
- optionally attach a note/URL;
- increase mastery only through PASS.

## I. Dependency/unlock conditions

A skill may have zero or more prerequisite conditions.

MVP condition types:
- prerequisite skill goal has status ACHIEVED;
- prerequisite skill mastery is at least level N.

All configured conditions must be satisfied for the dependent skill to become Available.

## J. Maintenance as a routine/habit

A learned skill can be placed into Maintenance mode.

A Maintenance Routine contains:
- a clear minimum action;
- one cadence;
- optional cue/context;
- completion history.

MVP cadence types:
1. **SELECTED_WEEKDAYS** — e.g. Mon/Thu or every day;
2. **TIMES_PER_WEEK** — e.g. any 3 days in a week;
3. **EVERY_N_DAYS** — e.g. at least once every 5 days.

No automatic mastery loss occurs.

Routine completion can:
- be marked directly with no XP, or
- be associated with a timed focus session, which earns normal XP/reward if eligible.

## K. Admin-managed templates with revision history

Administrator can create/edit/publish reusable content.

From the admin user's perspective, a published template can be edited. Internally, a published revision is never destructively overwritten:

1. admin chooses Edit on latest published template;
2. system clones that revision into a new draft revision;
3. admin changes/validates it;
4. publishing makes it the latest revision.

Rules:
- new users see the latest published revision;
- old revisions remain available for audit/rollback;
- an admin may mark an outdated revision superseded/hidden;
- an existing user's fork never changes silently;
- explicit merge/update assistance is post-MVP.

## L. Structured admin import

Structured JSON import is **SHOULD**, not a blocker.

Admin may paste/upload content into a draft, then:
- parse;
- validate references/enums/limits;
- detect cycles;
- preview/edit;
- explicitly publish.

This permits using external AI as a drafting tool without integrated AI.

## M. Data export

MVP export is versioned UTF-8 JSON.

Envelope fields:
- `schemaVersion`;
- `exportType`;
- `generatedAt`;
- `timezone`;
- exported content.

Supported exports:
1. one skill;
2. one skill + descendants/subtree;
3. one full path/tree;
4. complete account progression data.

Skill/tree exports may include:
- structure;
- current/historical goals;
- prerequisites;
- mastery configuration/attempts;
- quests;
- focus sessions or summaries;
- XP/time;
- maintenance routines;
- relevant reward metadata.

`ANALYSIS` export excludes direct account identifiers (Google subject, email, display name, avatar) by default so it can be pasted into an external AI.

CSV focus-history export is optional.

## N. Feedback / beta measurement

Users can submit free-text feedback.

No dedicated analytics SDK/dashboard is required.

Beta behavior can be evaluated from first-party domain records already necessary for product operation.

# 13. Explicitly Out of MVP Scope

- integrated LLM/AI generation;
- AI mastery evaluation;
- AI-generated quests/trees;
- third-party behavioral analytics platforms;
- user-facing analytics dashboards/advanced charts;
- social feed/friends/guilds;
- leaderboards;
- public profiles;
- chat;
- native mobile apps;
- payment processing;
- bank integration;
- marketplace;
- purchasable virtual currency;
- advanced avatar/equipment/loot/gacha;
- elaborate cosmetics catalog;
- achievement system;
- custom themes;
- advanced animation systems;
- 3D/WebGL skill tree;
- course integrations;
- calendar sync;
- punitive streaks;
- automatic mastery decay;
- external certification;
- team/company features;
- automatic mutation of user forks when a template changes.

Optional attractive charts or lightweight animations may be added only after MUST requirements if they do not delay beta.

# 14. Business Requirements

| ID | Requirement | Priority | Verification |
|---|---|---|---|
| BR-01 | Every active trainable skill must have one concrete measurable/verifiable current goal. | MUST | Skill cannot become Active without a valid goal. |
| BR-02 | Represent practice separately from capability/mastery. | MUST | XP can rise without mastery changing. |
| BR-03 | Connect every credited minute to one specific leaf skill. | MUST | Parent/category cannot receive direct focus XP. |
| BR-04 | Preserve uninterrupted focus while preventing forgotten timers from silently generating absurd progress. | MUST | Ordinary stopwatch + >10h REVIEW_REQUIRED rule. |
| BR-05 | Give immediate feedback after confirmed focus. | MUST | Finish/review updates XP/reward/history. |
| BR-06 | Support optional explicit mastery criteria where staged competence is meaningful. | MUST | Skill may enable/disable mastery. |
| BR-07 | Support goal- and mastery-based prerequisite conditions. | MUST | Dependent skill unlocks from configured conditions. |
| BR-08 | Support editable personal paths. | MUST | Fork is independently editable. |
| BR-09 | Support maintenance through recurring minimum routines without mastery decay. | MUST | Due/completion states work. |
| BR-10 | Support bounded personal reward credit from eligible focus. | MUST | €1/20 cumulative eligible min, €5/day cap. |
| BR-11 | Support machine-readable data portability. | MUST | Skill/subtree/tree/account JSON export works. |
| BR-12 | External users can complete core journey without founder intervention. | MUST | Usability test succeeds unaided. |
| BR-13 | Curated content remains centrally correctable through revisions. | MUST | Admin edits create revision; forks stay unchanged. |
| BR-14 | MVP remains usable without integrated AI. | MUST | No core journey depends on LLM. |
| BR-15 | Personal progression is private, exportable, and deletable. | MUST | Ownership/export/deletion tests pass. |
| BR-16 | Product should create recognizable game/progression feeling without requiring a full RPG. | SHOULD | Beta feedback confirms the framing is perceived. |

# 15. Stakeholder Requirements

- **SR-01:** A new user needs a low-friction path to first meaningful progress.
- **SR-02:** An active learner needs to know what they train, what they practiced, their current mastery, and what unlocks next.
- **SR-03:** Users need control over forked templates.
- **SR-04:** Users need a clear distinction between game progression and personal reward credit.
- **SR-05:** Administrator needs to maintain templates without direct DB edits.
- **SR-06:** Product owner needs behavioral data sufficient to judge the core proposition.


# 16. Functional Requirements

## Authentication/account

- **FR-001 MUST:** Google sign-in.
- **FR-002 MUST:** Sign out/end local session.
- **FR-003 MUST:** Server-side ownership isolation.
- **FR-004 MUST:** Self-service account deletion with confirmation.
- **FR-005 MUST:** Retain only required Google identity fields; no Google API tokens unless required later.
- **FR-006 MUST:** Store editable user IANA timezone; propose browser timezone on first use.

## Paths/skills/goals

- **FR-010 MUST:** Create blank path.
- **FR-011 MUST:** Fork published template revision.
- **FR-012 MUST:** Edit personal path.
- **FR-013 MUST:** Create nested skill.
- **FR-014 MUST:** Edit/archive/delete with dependency safety.
- **FR-015 MUST:** Parent-child organization.
- **FR-016 MUST:** Only leaf skills are directly trainable.
- **FR-017 MUST:** Reject hierarchy/prerequisite cycles.
- **FR-018 MUST:** One ACTIVE goal maximum per trainable skill.
- **FR-019 MUST:** Active goal contains a verifiable success criterion.
- **FR-019A SHOULD:** Baseline, target/unit, target date.
- **FR-019B MUST:** Goal history retains ACHIEVED/REPLACED/ABANDONED goals.
- **FR-019C MUST:** Goal achievement is explicitly confirmed by the user; the system does not infer success from XP/time alone.

## Mastery/dependencies

- **FR-020 MUST:** Mastery can be enabled/disabled per skill.
- **FR-021 MUST:** Enabled mastery uses fixed 0–4 default scale.
- **FR-022 MUST:** Criteria CRUD for levels 1–4.
- **FR-023 MUST:** Show next criterion.
- **FR-024 MUST:** Record PASS/FAIL attempt.
- **FR-025 MUST:** Sequential progression.
- **FR-026 SHOULD:** Optional evidence note/URL.
- **FR-027 MUST:** XP never changes mastery.
- **FR-028 MUST:** Prerequisite condition type `GOAL_ACHIEVED`.
- **FR-029 MUST:** Prerequisite condition type `MASTERY_AT_LEAST`.

## Focus stopwatch

- **FR-030 MUST:** Start focus on one active leaf skill.
- **FR-031 MUST:** Optional quest association.
- **FR-032 MUST:** Server stores start time; refresh/reopen recovers active session.
- **FR-033 MUST:** Only one active session per user.
- **FR-034 MUST:** Normal finish uses elapsed duration as default confirmed duration.
- **FR-035 MUST:** User may manually correct confirmed minutes.
- **FR-036 MUST:** At elapsed >10h, session enters REVIEW_REQUIRED and receives no automatic XP/reward.
- **FR-037 MUST:** REVIEW_REQUIRED can be corrected to 0–600 credited minutes or discarded.
- **FR-038 MUST:** Duplicate finish/review requests cannot double-award.
- **FR-039 SHOULD:** Session note.
- **FR-039A OUT:** No mandatory periodic notification/check-in in MVP.

## XP/levels

- **FR-040 MUST:** 1 confirmed minute = 1 XP.
- **FR-041 MUST:** XP credited only to selected leaf.
- **FR-042 MUST:** Parent/path aggregates are derived and never double-count character XP.
- **FR-043 MUST:** Character XP equals lifetime confirmed minutes.
- **FR-044 MUST:** Character level 1–100 uses the normalized exponential formula specified in Section 12.
- **FR-045 MUST:** Level 100 threshold = 600,000 XP.
- **FR-046 MUST:** Lifetime XP/time can exceed 600,000 while visual level remains 100.
- **FR-047 MUST:** No quest/goal/mastery bonus XP in MVP.

## Reward economy

- **FR-050 MUST:** Trainable skill has reward-eligible setting.
- **FR-051 MUST:** Eligible focused minutes accumulate per user-local calendar day.
- **FR-052 MUST:** Each cumulative 20 eligible minutes earns €1.
- **FR-053 MUST:** Maximum earned credit is €5 per local calendar day.
- **FR-054 MUST:** Session boundaries do not reset partial daily reward minutes.
- **FR-055 MUST:** Manual correction recalculates the affected day's earned credit consistently.
- **FR-056 MUST:** Reward balance derives from immutable/adjusting ledger transactions.
- **FR-057 MUST:** User can record spend/debit with description.
- **FR-058 MUST:** UI explains this is personal bookkeeping, not stored money.
- **FR-059 MUST:** Reward spending cannot reduce the tracked reward balance below zero.

## Quests

- **FR-060 MUST:** Quest CRUD/archive/complete.
- **FR-061 MUST:** Quest belongs to one trainable skill.
- **FR-062 MUST:** Source USER/TEMPLATE.
- **FR-063 MUST:** Completion does not grant XP/mastery.
- **FR-064 MUST:** Templates may provide starter quests.
- **FR-065 SHOULD:** Quest indicates which goal/criterion it supports.

## Maintenance routines

- **FR-070 MUST:** User can place skill in Maintenance mode.
- **FR-071 MUST:** Define minimum action.
- **FR-072 MUST:** Support cadence SELECTED_WEEKDAYS.
- **FR-073 MUST:** Support cadence TIMES_PER_WEEK.
- **FR-074 MUST:** Support cadence EVERY_N_DAYS.
- **FR-075 SHOULD:** Optional cue/context.
- **FR-076 MUST:** Compute due state from cadence/completions.
- **FR-077 MUST:** Missing routine never lowers mastery.
- **FR-078 MUST:** Direct routine completion can record no XP.
- **FR-079 MUST:** Timed maintenance practice uses normal focus/XP/reward rules.

## Templates/admin

- **FR-080 MUST:** Admin authorization.
- **FR-081 MUST:** Template draft/editor.
- **FR-082 MUST:** Template content can include skills, suggested goals, prerequisites, mastery settings/criteria, starter quests, maintenance suggestions, descriptive examples.
- **FR-083 MUST:** Validate before publish.
- **FR-084 MUST:** Editing published template clones latest published revision into new draft.
- **FR-085 MUST:** Publishing new revision does not mutate existing forks.
- **FR-086 MUST:** Revision history is inspectable; outdated revision can be superseded/hidden.
- **FR-087 SHOULD:** Structured JSON draft import.

## Export/privacy/feedback

- **FR-090 MUST:** Versioned JSON export of one skill.
- **FR-091 MUST:** Versioned JSON export of subtree.
- **FR-092 MUST:** Versioned JSON export of full path/tree.
- **FR-093 MUST:** Full-account structured export.
- **FR-094 MUST:** Analysis export excludes direct identity fields by default.
- **FR-095 SHOULD:** CSV focus-history export.
- **FR-096 MUST:** Free-text feedback.
- **FR-097 MUST:** Account deletion removes active personal data and invalidates sessions; backup expiration follows documented retention.
- **FR-098 OUT:** No third-party product-analytics SDK required.
- **FR-099 OUT:** No user-facing analytics dashboard required.

# 17. Core Business Rules

1. Every active trainable leaf skill has exactly one current measurable/verifiable goal.
2. Broad aspirations/financial outcomes are not valid skill endpoints by themselves.
3. Goal completion and mastery are separate concepts.
4. Mastery is optional per skill.
5. Where enabled, mastery progresses sequentially: Unknown → Guided → Independent → Defensible → Evidence.
6. **1 confirmed focused minute = 1 XP.**
7. Direct XP belongs only to the leaf skill actually trained.
8. Parent/category XP/time is aggregation only.
9. Character XP contains no quest/goal/mastery bonus in MVP.
10. Character level is 1–100 using the Section 12 normalized exponential formula.
11. Level 100 = 600,000 XP = 10,000 confirmed focus hours.
12. Visual level caps at 100; lifetime XP/time may continue.
13. Focus uses an ordinary stopwatch with no forced periodic interruptions.
14. Elapsed sessions over 10 hours are REVIEW_REQUIRED and produce no automatic credit.
15. Manual correction is trusted; a single session can credit at most 600 minutes.
16. Reward applies only to reward-eligible skills.
17. Daily reward = €1 per cumulative 20 eligible confirmed minutes, maximum €5/day in the user's configured timezone.
18. Reward balance is ledger-based.
19. Prerequisites may require goal achievement or mastery level.
20. Maintenance is a recurring minimum-action routine, not automatic competence loss.
21. Forks never change silently after source-template revisions.
22. Published template edits create new revisions instead of destructive overwrite.
23. Progression-critical calculations and ownership checks are server-controlled.
24. The system remains fully usable without AI.

# 18. Core User Journeys

## Journey 1 — First value
Google sign-in → choose template/blank path → select leaf skill → define measurable goal → start stopwatch → finish → confirm/correct time → see XP/reward/history.

Target setup before focus: **≤5 minutes** for template users.

## Journey 2 — Forgotten timer
User starts focus → forgets it → elapsed exceeds 10h → session becomes REVIEW_REQUIRED → user enters actual focused minutes (max 600) or discards → only then are XP/reward calculated.

## Journey 3 — Goal-driven training
Open skill → see current goal → choose quest/next action → practice → update progress → achieve/replace goal → optionally create next goal.

## Journey 4 — Mastery
For mastery-enabled skill: practice → inspect criterion → attempt → PASS/FAIL → on PASS increment mastery → recompute dependency conditions.

## Journey 5 — Rewards
Eligible focus accumulates daily minutes → every 20 minutes adds €1 until €5/day cap → user records real-world spend → ledger updates.

## Journey 6 — Maintenance
Skill enters Maintenance mode → user defines minimum action/cadence → routine becomes due → user completes directly or via timed practice → no mastery loss.

## Journey 7 — Template revision/fork
User forks published revision → admin later edits template through a new revision → new users see latest → old fork remains unchanged.

## Journey 8 — Export
User exports skill/subtree/path/account JSON → optional analysis export removes identity → data can be backed up or given to external AI.

# 19. Business Information Model

This is not the database schema.

- **User** — owns private progression and timezone.
- **Path** — broad development direction/category.
- **Skill** — organizational parent or trainable leaf capability.
- **Skill Goal** — one current measurable/verifiable endpoint for a trainable skill.
- **Goal History** — achieved/replaced/abandoned endpoints.
- **Prerequisite Condition** — GOAL_ACHIEVED or MASTERY_AT_LEAST.
- **Mastery Configuration** — whether staged assessment is used.
- **Mastery Criterion** — requirement for a mastery level.
- **Mastery Attempt** — PASS/FAIL.
- **Quest** — concrete training action.
- **Focus Session** — stopwatch event with active/completed/review state.
- **XP** — confirmed practice signal.
- **Reward Eligibility** — whether focused minutes for the skill participate in reward calculation.
- **Reward Transaction** — personal bookkeeping credit/debit/adjustment.
- **Maintenance Routine** — minimum action + cadence + optional cue.
- **Maintenance Completion** — completion history.
- **Template** — reusable admin-managed content.
- **Template Revision** — versioned historical snapshot; published revisions are immutable records, while a new draft revision remains editable until publication.
- **Export Package** — versioned structured data.
- **Feedback** — beta-user feedback.

# 20. Provenance

MVP content sources:

### Skills / goals / criteria / quests / templates
- USER
- TEMPLATE

Future-compatible:
- AI

Template content may later include **reference profiles / skillsets inspired by well-known people or fictional characters**, because the generic template model already supports this without special progression logic.

Examples might be "skillset inspired by [public figure/character]" or references inside a skill description to people/characters strongly associated with that capability.

Guardrails:
- such profiles are editorial interpretations, not objective measurements or endorsements;
- real-person profiles should rely on public biographical evidence;
- avoid unsupported negative claims about real people ("has no X skill");
- copyrighted/trademarked character imagery/text must not be reused without appropriate rights;
- no special reference-profile feature is required for MVP; ordinary admin-authored templates/descriptions are sufficient.

System-owned state—IDs, ownership, timestamps, XP, actual mastery, reward balance, focus history—cannot be supplied as arbitrary imported/generated content.

# 21. Non-Functional Requirements

- **NFR-01 MUST Reliability:** core progression cannot be silently lost.
- **NFR-02 MUST Focus robustness:** forgotten/open sessions cannot silently accrue unlimited XP/reward.
- **NFR-03 MUST Data integrity:** focus completion/correction, XP, and reward adjustments remain transactionally consistent.
- **NFR-04 MUST Security:** Google identity is validated; server authorization/ownership protects all private resources; secrets are managed securely.
- **NFR-05 MUST Validation:** user/admin/import inputs are validated server-side.
- **NFR-06 MUST Idempotency:** duplicate focus-finalization requests cannot duplicate XP/reward.
- **NFR-07 MUST Error handling:** understandable errors; no sensitive stack traces.
- **NFR-08 SHOULD Performance:** ordinary API operations should feel immediate; target p95 under ~500 ms where practical.
- **NFR-09 SHOULD Availability:** stable enough for a 14-day external beta.
- **NFR-10 MUST Observability:** production logs/monitoring sufficient to diagnose core failures.
- **NFR-11 MUST Privacy:** skills, goals, notes, evidence, exports, and progression are private by default.
- **NFR-12 MUST Data portability:** structured export is versioned and reproducible.
- **NFR-13 MUST Account deletion:** active user data can be deleted; backup-retention behavior is documented.
- **NFR-14 SHOULD Responsive web:** common desktop/mobile browser widths.
- **NFR-15 SHOULD Accessibility baseline:** keyboard access, visible focus states, contrast, labels.
- **NFR-16 SHOULD Backup/recovery:** practical recovery before external beta.
- **NFR-17 MUST No AI dependency:** no core function requires AI.
- **NFR-18 MUST Minimal tracking:** avoid non-essential third-party tracking/cookies.
- **NFR-19 MUST Timezone consistency:** reward-day calculations use the user's stored IANA timezone.

# 22. UX Requirements

- **UX-01:** Make XP = practice, Mastery = ability, Reward = reward credit visually obvious.
- **UX-02:** Templates reduce setup burden.
- **UX-03:** Use progressive disclosure for prerequisites/maintenance.
- **UX-04:** Game language cannot obscure real-world meaning.
- **UX-05:** Focus completion gives immediate result summary.
- **UX-06:** No punishment-first design or automatic mastery downgrade.
- **UX-07:** User can edit their own path; templates are starting points.


# 23. Minimal Game Layer

Included:
- character XP;
- character level;
- skill XP;
- mastery labels;
- locked/unlocked states;
- basic game-inspired visual language;
- lightweight completion/level-up feedback.

Excluded:
- equipment;
- inventory;
- loot;
- collectible characters;
- purchasable cosmetics;
- gacha;
- combat;
- parties.

The first MVP must prove that **real progression itself is satisfying**.


# 24. Admin and Content Strategy

## Why admin exists

Users should not have to know how to decompose every goal perfectly. At the same time, integrated AI is intentionally excluded from MVP.

Curated template content is initially authored manually/admin-side. External AI tools may be used by the administrator to draft structured content, but every draft remains editable and reviewable before publication.

## Template lifecycle

Operationally:

DRAFT → VALIDATED → PUBLISHED → REVISED / SUPERSEDED

"Published" does **not** mean an admin can never correct it.

Instead:
- admin edits produce a new revision;
- latest revision can be presented to new users;
- prior revisions are retained for audit/rollback;
- existing user forks do not silently mutate.

## Initial content

Start with the founder's real trees because they can be dogfooded deeply.

Then add a small number of broader/popular templates based on observed interest.

The template system is generic enough to support:
- domain paths (Backend Developer, German Speaking, Strength);
- goal-specific paths;
- editorial "skillset inspired by X" profiles.

No large template catalog is required before validation.

## Structured import

If implementation remains straightforward:

1. admin pastes/uploads versioned JSON;
2. system parses into Draft;
3. validates IDs/references/enums/limits;
4. detects cycles;
5. previews/edit;
6. admin confirms;
7. revision is published.

This is the intended future integration seam for AI Structured Outputs.

# 25. Validation Plan

## Stage A — Founder dogfooding

Minimum 7 active-use days with the real system.

Purpose:
- test measurable-goal workflow;
- intentionally test a forgotten stopwatch and REVIEW_REQUIRED recovery;
- validate normal finish/manual correction;
- validate XP/reward calculations;
- use at least one mastery-enabled and one mastery-disabled skill;
- use export;
- convert at least one skill to a maintenance routine;
- fix obvious usability/data-integrity problems.

Dogfooding is a quality gate, not market validation.

## Stage B — External beta

Initial cohort:
- acquaintances/friends who plausibly fit the use case;
- additional users reached through the founder's media/content if available.

Target initial size: roughly **10–20 usable participants**, but quality of actual use matters more than reaching a round number.

Observation window: **14 days**.

Users get a short product description and access. The core workflow should not require a private live tutorial.

# 26. MVP Metrics

No dedicated analytics product/SDK is required.

Metrics are derived from first-party domain records and manual/admin queries.

## Activation
Activated user:
1. Google sign-in;
2. path created/forked;
3. measurable goal set;
4. first focus session completed/reviewed;
5. resulting XP viewed.

Directional target: **≥60%** of users who meaningfully begin onboarding.

## Time-to-value
Template setup before first focus: target **≤5 minutes**.

## Core-loop use
Among activated users: directional target median **≥3 completed focus sessions in first 7 days**.

## D7 return
Meaningful return in Day 6–8 window.
Directional target: **≥35%** of activated users.

For tiny cohorts, inspect raw behavior rather than treating percentages as statistical proof.

## Goal engagement
Do users revisit/update/achieve goals rather than treating them as onboarding paperwork?

## Stopwatch quality
How often do sessions become REVIEW_REQUIRED?
How often are completed sessions manually corrected?
Does the 10-hour threshold resolve forgotten-timer errors without interrupting normal focus?

## Mastery engagement
Among users with mastery-enabled skills, do they inspect criteria/attempt progression?

## Path engagement
Does the tree influence what gets trained next?

## Reward engagement
Do users enable reward on selected skills, earn credit, and actually record spending?

## Maintenance engagement
Do due routines produce real maintenance actions?

## Export engagement
Do users export data for backup/analysis, especially external AI analysis?

## Qualitative questions
- What would you use instead?
- Does requiring a concrete goal help or annoy you?
- Did you forget the timer, and was correction understandable?
- Is mastery useful on the skills where you enabled it?
- Does the tree affect your next action?
- Does reward improve motivation or add bookkeeping?
- Would you keep using this without reminders?
- What feels unnecessary?

# 27. Go / Iterate / Stop

## GO

Continue investing if most are true:
- activation is healthy for the small cohort;
- users return in Week 1;
- users repeatedly record real focus;
- measurable goals remain relevant after onboarding;
- mastery/path affects behavior for a meaningful subset;
- some users voluntarily use reward/maintenance/export;
- users articulate value beyond "nice timer";
- reliability is not contaminating behavior.

## ITERATE

Iterate if:
- goals are useful but too annoying to define;
- forgotten-session correction is confusing or too frequent;
- the 10-hour threshold is clearly too high/low;
- focus loop works but tree/mastery is confusing;
- templates materially reduce setup;
- reward helps only some user types;
- maintenance needs a simpler routine model;
- usability/reliability clearly explains weak usage.

## STOP / PIVOT

Do not build the grand RPG if:
- reliable users still prefer existing timer/task/note tools;
- measurable goals/mastery are viewed as administrative overhead;
- users consistently stop logging focus;
- tree/path is decorative rather than decision-support;
- novelty disappears and voluntary return is very low;
- only cosmetic/anime presentation is valued.

Weak core signal must not be "fixed" by adding AI, loot, social, or more animation.

# 28. Commercial Validation

The first closed beta primarily validates repeated value.

Payment is not required yet.

If retention is promising, the next phase tests:
- willingness to pay;
- pricing model;
- whether users pay for software, templates/content, or both.

No payment integration before evidence of recurring use.


# 29. Constraints

- **C-01 Solo development:** scope must fit one developer.
- **C-02 Learning project:** Java/Spring practice is a constraint/secondary benefit, not product value.
- **C-03 Multi-user MVP:** MVP must be usable by external users, not only the founder.
- **C-04 Web-first:** deploy as web product.
- **C-05 Limited content:** start with founder-authored trees, then add a small number of popular templates.
- **C-06 Small beta:** acquaintances + media-sourced users; combine raw behavior with feedback.
- **C-07 No integrated AI dependency:** AI comes later.
- **C-08 Trust-based self-tracking:** manual correction/self-assessment is allowed; the product does not attempt anti-cheat.
- **C-09 Privacy simplicity:** avoid non-essential tracking/cookies in MVP where possible.

# 30. Known Implementation Constraints

- Backend: Java / Spring Boot.
- Persistence: relational DB; PostgreSQL preferred.
- Authentication: Google OAuth/OpenID Connect.
- Server enforces authorization, confirmed duration, XP, reward-day cap, session review, and progression rules.
- Only leaf skills can receive direct focus XP.
- Focus uses an ordinary persistent stopwatch; no periodic forced check-ins.
- Elapsed sessions over 10 hours require manual review before credit.
- Export is versioned JSON first.
- Real beta deployment is required.
- CI/tests must protect progression, reward, focus idempotency, template revision, auth, export, and ownership rules.
- Draft/import model remains compatible with future structured AI output.

Frontend technology is not mandated by this BRD.

# 31. Risks

| Risk | Impact | MVP mitigation |
|---|---|---|
| Product becomes complicated self-management | High | Templates, progressive disclosure, narrow onboarding |
| Mandatory goals feel bureaucratic | High | Require one concise measurable endpoint; templates suggest examples |
| Users game XP/manual corrections | Low/Medium | Trust model; XP is self-tracking, not competitive value |
| User forgets timer | High | Persistent stopwatch + >10h REVIEW_REQUIRED + manual correction |
| 10-hour sanity threshold does not fit real behavior | Medium | Dogfood deliberately; measure review/correction frequency; adjust constant if needed |
| Self-assessed mastery inconsistent | Medium | Mastery optional; explicit criteria + optional evidence |
| Curated criteria/templates are poor | High | Admin revisioning; founder dogfooding; small catalog |
| Tree setup too much work | High | Forkable templates |
| Reward ledger feels artificial | Medium | Per-skill eligibility; measure actual use; keep separate from mastery |
| Maintenance becomes another habit tracker | Medium | Narrow maintenance-routine feature only |
| Famous-person/character templates create legal/content problems | Medium | Editorial disclaimers, public sources, no unlicensed assets, avoid unsupported negative real-person claims |
| Scope explodes into full RPG | High | Explicit out-of-scope list |
| AI added too early | High | External AI only via structured admin import during MVP |
| Data loss invalidates beta | High | Transactions, backups, observability |
| Privacy/legal omissions block public use | High | Privacy notice, deletion/export, minimal cookies/tracking, pre-beta legal checklist |

# 32. Dependencies

Depends on:
- deployable backend/database;
- usable web frontend;
- auth;
- small seed template set;
- analytics;
- beta users;
- stable mastery/focus/reward definitions.

Does not depend on:
- LLM provider;
- payment provider;
- social network;
- native app stores;
- large content catalog.


# 33. Transition Requirements Before Closed Beta

Before external beta:

1. production-like deployment;
2. Google sign-in works;
3. ownership isolation verified;
4. user timezone captured/editable;
5. measurable-goal workflow works;
6. normal stopwatch + >10h REVIEW_REQUIRED flow works;
7. XP formula and Level 100 threshold tested;
8. reward eligibility + cumulative 20-minute calculation + €5/day cap tested;
9. mastery-disabled and mastery-enabled skills both work;
10. goal/mastery prerequisite conditions work;
11. maintenance cadence types work;
12. seed templates exist;
13. admin can publish revised templates without mutating forks;
14. skill/subtree/path/account JSON export works;
15. account deletion works;
16. production logs exist;
17. backup/recovery approach exists;
18. feedback channel exists;
19. privacy notice is published;
20. Google authentication/data-processing disclosure is included;
21. only necessary cookies/storage are used unless consent handling is added;
22. legal notice/Impressum applicability is checked for the actual launch context.

# 34. MVP Release Acceptance Gate

## Account/privacy
- Google sign-in/sign-out work;
- cross-user access is prevented;
- timezone is stored/edited;
- account deletion works;
- privacy notice is accessible.

## Goals/skills
- only leaves receive direct focus;
- active leaf requires one current goal;
- achieved/replaced goals retain history;
- skill can operate with mastery disabled.

## Template/path
- fork works;
- personal edit works;
- admin edit creates new template revision;
- old fork remains unchanged.

## Focus
- normal stopwatch survives refresh/reopen;
- only one active session exists;
- normal finish credits exactly once;
- elapsed >10h produces REVIEW_REQUIRED with zero automatic credit;
- corrected review supports 0–600 minutes;
- duplicate requests cannot double-award.

## XP/levels
- 1 confirmed minute = 1 XP;
- parent aggregation does not duplicate XP;
- level formula matches known threshold test cases;
- Level 100 = exactly 600,000 XP;
- lifetime XP can exceed the visual cap.

## Reward
- only eligible skills contribute;
- eligible minutes accumulate across same-day sessions;
- each cumulative 20 minutes = €1;
- cap = €5/user-local day;
- correction recalculates with ledger adjustment;
- balance equals ledger sum.

## Mastery/prerequisites
- mastery can be disabled;
- PASS increments one level where enabled;
- FAIL does not;
- XP does not change mastery;
- GOAL_ACHIEVED prerequisite works;
- MASTERY_AT_LEAST prerequisite works.

## Maintenance
- all three cadence modes work;
- direct completion can record no XP;
- timed maintenance can use normal focus;
- missed routine never lowers mastery.

## Export
- skill/subtree/path/account JSON validates against export schema;
- analysis export strips direct identity fields.

## Admin
- invalid/cyclic content cannot publish;
- revision history is retained;
- latest revision can be superseded without destructive overwrite.

## Product quality
- no known critical data-loss defect;
- no known authorization bypass;
- major error states have usable UI;
- external user can complete core journey without developer tools.

# 35. Traceability

| Hypothesis / Objective | Main requirements |
|---|---|
| H1 Concrete-goal value | BR-01, FR-018–019B |
| H2 Practice vs capability | BR-02, FR-020–027 |
| H3 Stopwatch reliability | BR-04, FR-030–039A |
| H4 Reward motivation | BR-05, BR-10, FR-050–058 |
| H5 Skill-path value | BR-07, BR-08, FR-010–017, FR-028–029 |
| H6 Setup friction | FR-011, FR-080–087, UX-02 |
| H7 Self-assessed mastery | FR-020–027 |
| H8 Maintenance routine | BR-09, FR-070–079 |
| OBJ-03 Time-to-value | Journey 1, activation/time-to-value metrics |
| OBJ-04 Beta evidence | Section 26 + first-party domain records |
| OBJ-05 Data ownership | BR-11, BR-15, FR-090–097 |
| OBJ-06 Scope control | Section 13, Section 23, Section 27 |

# 36. Priority Summary

## MUST
- Google auth/data ownership/timezone;
- paths and leaf-vs-parent skill model;
- one current measurable goal per active leaf;
- optional mastery;
- goal/mastery prerequisite conditions;
- ordinary persistent stopwatch;
- >10h REVIEW_REQUIRED flow;
- manual time correction;
- 1 XP/minute;
- 100-level exponential character curve;
- Level 100 = 600k XP;
- reward eligibility per skill;
- cumulative €1/20 eligible minutes;
- €5/day cap;
- reward ledger/spending;
- quests;
- maintenance routines with three cadence modes;
- admin templates + revision history;
- fork independence;
- versioned JSON skill/subtree/path/account export;
- account deletion/privacy basics;
- feedback;
- validation/idempotency/error handling/observability.

## SHOULD
- evidence note/URL;
- structured admin import;
- responsive mobile browser;
- accessibility baseline;
- backup/recovery automation;
- CSV focus-history export;
- optional maintenance cue/context.

## COULD
- non-blocking long-session warning;
- simple rank label;
- lightweight level-up animation;
- attractive charts after core work;
- editorial "skillset inspired by X" templates through the generic template system.

# 37. Cost-Benefit Assessment

## Expected costs
Primary: founder/developer time.

Secondary:
- hosting;
- database;
- domain;
- monitoring/analytics;
- possible design assets.

Avoided in MVP:
- LLM inference;
- payment infrastructure;
- native app distribution;
- large content production.

## Expected benefits if validated
- evidence of demand;
- actual daily-use product;
- foundation for future AI-assisted generation;
- reusable template/content system;
- possible commercial direction;
- strong technical portfolio evidence as a secondary benefit.

**Technical sophistication alone is not success.**


# 38. Post-MVP Candidates

Unordered until evidence exists:

- integrated AI-generated SkillTreeDraft using strict structured schema;
- AI reviewer for tree/goal/criterion quality;
- AI-generated quests;
- richer curated template catalog;
- public/community template publishing;
- explicit template update/merge assistant;
- reference/exemplar metadata for skills;
- more "skillset inspired by X" editorial profiles;
- deeper anime/RPG visual system;
- titles;
- cosmetics;
- achievements;
- advanced maintenance/freshness models if routines prove insufficient;
- native app;
- richer notification scheduling;
- calendar integration;
- social/accountability;
- subscription/payment validation;
- user-facing analytics/beautiful charts if they create actual value.

# 39. Open Business Decisions Before Detailed Design

Resolved for MVP:

1. **Working codename:** `Focus Reward`; final brand name is a later branding decision and does not block design.
2. **Authentication:** Google sign-in.
3. **User timezone:** browser-proposed IANA timezone, editable by user.
4. **Focus model:** ordinary stopwatch; no periodic forced check-ins.
5. **Forgotten timer:** elapsed >10h → REVIEW_REQUIRED; no automatic XP/reward; manual correction up to 600 credited minutes or discard.
6. **XP:** 1 XP per confirmed focused minute.
7. **Direct training:** only leaf skills receive direct XP.
8. **Character levels:** 100 levels using normalized exponential `k=0.05`; Level 100 = exactly 600,000 XP / 10,000 hours.
9. **Reward:** cumulative €1 per 20 reward-eligible minutes in the user's local calendar day; max €5/day.
10. **Reward eligibility:** configurable per trainable skill.
11. **Manual corrections:** trusted; XP/reward is recalculated consistently.
12. **Goals:** exactly one current measurable/verifiable goal per active trainable skill; goal history retained.
13. **Mastery:** optional per skill; default 0–4 framework where enabled.
14. **Prerequisites:** GOAL_ACHIEVED and MASTERY_AT_LEAST.
15. **Maintenance:** minimum action + one of SELECTED_WEEKDAYS / TIMES_PER_WEEK / EVERY_N_DAYS + optional cue; no mastery decay.
16. **Templates:** founder's real paths first, broader templates later.
17. **Template editing:** published content is editable through new revisions; existing forks never change silently.
18. **Structured admin import:** SHOULD be included if low-complexity.
19. **Analytics:** no dedicated SDK/dashboard in MVP; use first-party records + feedback.
20. **Export:** versioned UTF-8 JSON for skill/subtree/path/account; analysis export removes direct identity.
21. **Google data retained:** provider subject ID, email/email-verified, display name, optional avatar; no API tokens unless later needed.
22. **Beta recruitment:** acquaintances/friends plus users reached through founder media/content.
23. **Privacy:** privacy notice, account deletion/export, minimal tracking/cookies, and applicable legal-notice requirements before external beta.

No unresolved business decision currently blocks Solution/Domain Design.

Items intentionally deferred because they are implementation/branding choices rather than business gaps:
- final product brand/domain;
- exact JSON field-level schema;
- database schema/API design;
- frontend technology/layout;
- whether to add a passive long-session warning;
- whether first beta includes a celebrity/fictional-character-inspired editorial template.

# 40. Definition of MVP Success

After a reliable external 14-day beta, the MVP must let us answer:

1. Do users create specific measurable goals and continue referring to them?
2. Do users repeatedly record real focused practice with the stopwatch?
3. Does the 10-hour review mechanism handle forgotten timers without interrupting deep work?
4. For skills where mastery is enabled, is it useful separately from XP?
5. Does the path/dependency model affect what users train next?
6. Does the bounded reward mechanic help motivation or add bookkeeping?
7. Do maintenance routines produce retention practice?
8. Is export useful for backup/reflection/external AI analysis?
9. Can users reach value quickly without founder assistance?
10. Do users voluntarily return after novelty wears off?
11. Is the differentiated benefit strong enough to justify another development cycle?

The MVP is not successful merely because it is feature-complete.

The implementation exists to answer these questions.


CREATE TABLE skill_goals
(
    id                UUID           NOT NULL,
    skill_id          UUID           NOT NULL,
    title             VARCHAR(160)   NOT NULL,
    success_criterion TEXT           NOT NULL,
    baseline_value    NUMERIC,
    target_value      NUMERIC,
    unit              VARCHAR(40),
    target_date       DATE,
    status            VARCHAR(255)   NOT NULL,
    outcome_note      TEXT,
    started_at        TIMESTAMPTZ(6),
    closed_at         TIMESTAMPTZ(6),
    created_at        TIMESTAMPTZ(6) NOT NULL,
    updated_at        TIMESTAMPTZ(6) NOT NULL,
    version           BIGINT,
    CONSTRAINT pk_skill_goals PRIMARY KEY (id)
);

ALTER TABLE skill_goals
    ADD CONSTRAINT FK_SKILL_GOALS_ON_SKILL FOREIGN KEY (skill_id) REFERENCES skill_nodes (id);

CREATE UNIQUE INDEX one_active_goal_per_skill_constraint
    ON skill_goals (skill_id)
    WHERE status = 'ACTIVE';
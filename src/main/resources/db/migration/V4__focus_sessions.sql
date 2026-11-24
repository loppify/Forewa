CREATE TABLE focus_sessions
(
    id                       UUID           NOT NULL,
    user_id                  UUID           NOT NULL,
    skill_id                 UUID           NOT NULL,
    status                   VARCHAR(255),
    started_at               TIMESTAMPTZ(6) NOT NULL,
    stopped_at               TIMESTAMPTZ(6),
    raw_elapsed_seconds      INTEGER,
    confirmed_seconds        INTEGER,
    credited_minutes         INTEGER        NOT NULL,
    reward_eligible_snapshot BOOLEAN        NOT NULL,
    reward_local_date        date           NOT NULL,
    timezone_snapshot        VARCHAR(255)   NOT NULL,
    note                     TEXT,
    corrected                BOOLEAN        NOT NULL,
    correction_note          TEXT,
    created_at               TIMESTAMPTZ(6),
    updated_at               TIMESTAMPTZ(6),
    version                  BIGINT,
    CONSTRAINT pk_focus_sessions PRIMARY KEY (id)
);

ALTER TABLE focus_sessions
    ADD CONSTRAINT FK_FOCUS_SESSIONS_ON_SKILL FOREIGN KEY (skill_id) REFERENCES skill_nodes (id);

ALTER TABLE focus_sessions
    ADD CONSTRAINT FK_FOCUS_SESSIONS_ON_USER FOREIGN KEY (user_id) REFERENCES user_accounts (id);

CREATE UNIQUE INDEX uq_focus_session_one_active_per_user
    ON focus_sessions(user_id)
    WHERE status = 'ACTIVE';

CREATE INDEX idx_focus_session_user_started
    ON focus_sessions(user_id, started_at DESC);

CREATE INDEX idx_focus_session_skill_started
    ON focus_sessions(skill_id, started_at DESC);

CREATE INDEX idx_focus_session_user_reward_date
    ON focus_sessions(user_id, reward_local_date);


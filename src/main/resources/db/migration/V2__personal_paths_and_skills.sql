CREATE TABLE personal_paths
(
    id                       UUID           NOT NULL,
    user_id                  UUID           NOT NULL,
    name                     VARCHAR(120)   NOT NULL,
    description              TEXT,
    source_template_id       UUID,
    source_template_revision INTEGER,
    created_at               TIMESTAMPTZ(6) NOT NULL,
    archived_at              TIMESTAMPTZ(6),
    updated_at               TIMESTAMPTZ(6) NOT NULL,
    version                  BIGINT         NOT NULL,
    CONSTRAINT pk_personal_paths PRIMARY KEY (id)
);

ALTER TABLE personal_paths
    ADD CONSTRAINT FK_PERSONAL_PATHS_ON_USER FOREIGN KEY (user_id) REFERENCES user_accounts (id);

CREATE INDEX idx_personal_paths_user_id
    ON personal_paths (user_id);
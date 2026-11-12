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


CREATE TABLE skill_nodes
(
    id                       UUID         NOT NULL,
    path_id                  UUID         NOT NULL,
    parent_id                UUID,
    node_type                VARCHAR(255) NOT NULL,
    name                     VARCHAR(120) NOT NULL,
    description              TEXT,
    sort_order               INTEGER      NOT NULL,
    reward_eligible          BOOLEAN      NOT NULL,
    mastery_enabled          BOOLEAN      NOT NULL,
    current_mastery_level    SMALLINT     NOT NULL,
    source_template_node_key VARCHAR(255),
    archived_at              TIMESTAMPTZ(6),
    created_at               TIMESTAMPTZ(6) NOT NULL,
    updated_at               TIMESTAMPTZ(6) NOT NULL,
    version                  BIGINT,
    CONSTRAINT pk_skill_node PRIMARY KEY (id)
);

ALTER TABLE skill_nodes
    ADD CONSTRAINT FK_SKILL_NODE_ON_PARENT FOREIGN KEY (parent_id) REFERENCES skill_nodes (id);

ALTER TABLE skill_nodes
    ADD CONSTRAINT FK_SKILL_NODE_ON_PATH FOREIGN KEY (path_id) REFERENCES personal_paths (id);
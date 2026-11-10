CREATE TABLE user_accounts
(
    id             UUID           NOT NULL,
    google_subject VARCHAR(255)   NOT NULL,
    email          VARCHAR(255)   NOT NULL,
    email_verified BOOLEAN        NOT NULL,
    display_name   VARCHAR(255),
    avatar_url     VARCHAR(255),
    timezone       VARCHAR(255),
    role           VARCHAR(255)   NOT NULL,
    created_at     TIMESTAMPTZ(6) NOT NULL,
    updated_at     TIMESTAMPTZ(6) NOT NULL,
    last_login_at  TIMESTAMPTZ(6) NOT NULL,
    deleted_at     TIMESTAMPTZ(6),
    version        BIGINT         NOT NULL,
    CONSTRAINT pk_user_accounts PRIMARY KEY (id)
);

ALTER TABLE user_accounts
    ADD CONSTRAINT uc_user_account_google_subject UNIQUE (google_subject);

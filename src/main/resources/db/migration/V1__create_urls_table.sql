-- URL sequences for performance (batch allocation)
CREATE SEQUENCE IF NOT EXISTS url_id_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Main URLs table
CREATE TABLE IF NOT EXISTS urls (
    id              BIGINT          PRIMARY KEY DEFAULT nextval('url_id_seq'),
    short_code      VARCHAR(20)     NOT NULL UNIQUE,
    original_url    VARCHAR(2048)   NOT NULL,
    created_at      TIMESTAMP       NOT NULL DEFAULT NOW(),
    expires_at      TIMESTAMP,
    click_count     BIGINT          NOT NULL DEFAULT 0,
    custom_alias    VARCHAR(100)    UNIQUE,
    active          BOOLEAN         NOT NULL DEFAULT TRUE,
    created_by_ip   VARCHAR(45)
);

-- Indexes for hot query paths
CREATE INDEX IF NOT EXISTS idx_urls_short_code  ON urls(short_code);
CREATE INDEX IF NOT EXISTS idx_urls_custom_alias ON urls(custom_alias) WHERE custom_alias IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_urls_active       ON urls(active) WHERE active = TRUE;
CREATE INDEX IF NOT EXISTS idx_urls_expires_at   ON urls(expires_at) WHERE expires_at IS NOT NULL;

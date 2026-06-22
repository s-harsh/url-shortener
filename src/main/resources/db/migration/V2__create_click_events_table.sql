-- Click event sequences for high write throughput
CREATE SEQUENCE IF NOT EXISTS click_event_id_seq
    START WITH 1
    INCREMENT BY 100
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

-- Click events for analytics
CREATE TABLE IF NOT EXISTS click_events (
    id          BIGINT          PRIMARY KEY DEFAULT nextval('click_event_id_seq'),
    url_id      BIGINT          NOT NULL REFERENCES urls(id) ON DELETE CASCADE,
    clicked_at  TIMESTAMP       NOT NULL DEFAULT NOW(),
    ip_address  VARCHAR(45),
    user_agent  VARCHAR(512),
    referer     VARCHAR(2048)
);

-- Indexes optimized for analytics queries
CREATE INDEX IF NOT EXISTS idx_click_events_url_id     ON click_events(url_id);
CREATE INDEX IF NOT EXISTS idx_click_events_clicked_at ON click_events(clicked_at);
CREATE INDEX IF NOT EXISTS idx_click_events_url_date   ON click_events(url_id, clicked_at DESC);

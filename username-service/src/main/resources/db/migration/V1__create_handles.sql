CREATE TABLE IF NOT EXISTS handles (
    handle VARCHAR(8) PRIMARY KEY,
    email TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX IF NOT EXISTS handles_email_idx ON handles (email);

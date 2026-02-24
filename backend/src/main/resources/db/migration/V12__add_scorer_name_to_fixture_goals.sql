ALTER TABLE fixture_goals
    ADD COLUMN IF NOT EXISTS scorer_name VARCHAR(255);

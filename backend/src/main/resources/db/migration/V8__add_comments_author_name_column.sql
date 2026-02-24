ALTER TABLE comments
    ADD COLUMN IF NOT EXISTS author_name VARCHAR(255);

UPDATE comments c
SET author_name = u.full_name
FROM users u
WHERE c.author_id = u.id
  AND c.author_name IS NULL;

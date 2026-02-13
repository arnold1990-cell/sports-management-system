DO $$
DECLARE
    content_type text;
BEGIN
    SELECT data_type
    INTO content_type
    FROM information_schema.columns
    WHERE table_schema = 'public'
      AND table_name = 'posts'
      AND column_name = 'content';

    IF content_type = 'bytea' THEN
        BEGIN
            ALTER TABLE posts
                ALTER COLUMN content TYPE text
                USING convert_from(content, 'UTF8');
        EXCEPTION
            WHEN character_not_in_repertoire OR untranslatable_character THEN
                ALTER TABLE posts ADD COLUMN IF NOT EXISTS content_text text;
                UPDATE posts SET content_text = encode(content, 'escape');
                ALTER TABLE posts ALTER COLUMN content_text SET NOT NULL;
                ALTER TABLE posts DROP COLUMN content;
                ALTER TABLE posts RENAME COLUMN content_text TO content;
        END;
    ELSIF content_type IS NOT NULL AND content_type <> 'text' THEN
        ALTER TABLE posts
            ALTER COLUMN content TYPE text USING content::text;
    END IF;
END;
$$;

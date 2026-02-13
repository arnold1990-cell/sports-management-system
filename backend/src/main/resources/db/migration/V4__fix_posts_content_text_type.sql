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
        ALTER TABLE posts ADD COLUMN content_text text;

        BEGIN
            UPDATE posts
            SET content_text = convert_from(content, 'UTF8');
        EXCEPTION
            WHEN character_not_in_repertoire OR untranslatable_character THEN
                UPDATE posts
                SET content_text = encode(content, 'escape');
        END;

        ALTER TABLE posts ALTER COLUMN content_text SET NOT NULL;
        ALTER TABLE posts DROP COLUMN content;
        ALTER TABLE posts RENAME COLUMN content_text TO content;
    END IF;
END;
$$;

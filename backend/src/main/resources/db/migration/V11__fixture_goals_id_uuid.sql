CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

ALTER TABLE fixture_goals
    ADD COLUMN IF NOT EXISTS id_uuid UUID;

UPDATE fixture_goals
SET id_uuid = uuid_generate_v4()
WHERE id_uuid IS NULL;

DO
$$
DECLARE
    rec RECORD;
    fk_not_null BOOLEAN;
    match_clause TEXT;
    on_update_clause TEXT;
    on_delete_clause TEXT;
    deferrable_clause TEXT;
BEGIN
    FOR rec IN
        SELECT c.conname,
               ns.nspname AS child_schema,
               cls.relname AS child_table,
               att.attname AS child_column,
               c.confupdtype,
               c.confdeltype,
               c.confmatchtype,
               c.condeferrable,
               c.condeferred,
               att.attnotnull AS child_not_null
        FROM pg_constraint c
                 JOIN pg_class cls ON cls.oid = c.conrelid
                 JOIN pg_namespace ns ON ns.oid = cls.relnamespace
                 JOIN pg_class parent_cls ON parent_cls.oid = c.confrelid
                 JOIN pg_namespace parent_ns ON parent_ns.oid = parent_cls.relnamespace
                 JOIN pg_attribute att ON att.attrelid = c.conrelid
            AND att.attnum = c.conkey[1]
        WHERE c.contype = 'f'
          AND parent_ns.nspname = current_schema()
          AND parent_cls.relname = 'fixture_goals'
          AND array_length(c.conkey, 1) = 1
          AND array_length(c.confkey, 1) = 1
    LOOP
        fk_not_null := rec.child_not_null;

        EXECUTE format(
                'ALTER TABLE %I.%I ADD COLUMN %I UUID',
                rec.child_schema,
                rec.child_table,
                rec.child_column || '_uuid'
                );

        EXECUTE format(
                'UPDATE %I.%I child SET %I = fg.id_uuid FROM %I.fixture_goals fg WHERE child.%I = fg.id',
                rec.child_schema,
                rec.child_table,
                rec.child_column || '_uuid',
                current_schema(),
                rec.child_column
                );

        EXECUTE format(
                'ALTER TABLE %I.%I DROP CONSTRAINT %I',
                rec.child_schema,
                rec.child_table,
                rec.conname
                );

        EXECUTE format(
                'ALTER TABLE %I.%I DROP COLUMN %I',
                rec.child_schema,
                rec.child_table,
                rec.child_column
                );

        EXECUTE format(
                'ALTER TABLE %I.%I RENAME COLUMN %I TO %I',
                rec.child_schema,
                rec.child_table,
                rec.child_column || '_uuid',
                rec.child_column
                );

        IF fk_not_null THEN
            EXECUTE format(
                    'ALTER TABLE %I.%I ALTER COLUMN %I SET NOT NULL',
                    rec.child_schema,
                    rec.child_table,
                    rec.child_column
                    );
        END IF;

        match_clause := CASE rec.confmatchtype
                            WHEN 'f' THEN ' MATCH FULL'
                            WHEN 'p' THEN ' MATCH PARTIAL'
                            ELSE ''
            END;

        on_update_clause := CASE rec.confupdtype
                                WHEN 'r' THEN ' ON UPDATE RESTRICT'
                                WHEN 'c' THEN ' ON UPDATE CASCADE'
                                WHEN 'n' THEN ' ON UPDATE SET NULL'
                                WHEN 'd' THEN ' ON UPDATE SET DEFAULT'
                                ELSE ' ON UPDATE NO ACTION'
            END;

        on_delete_clause := CASE rec.confdeltype
                                WHEN 'r' THEN ' ON DELETE RESTRICT'
                                WHEN 'c' THEN ' ON DELETE CASCADE'
                                WHEN 'n' THEN ' ON DELETE SET NULL'
                                WHEN 'd' THEN ' ON DELETE SET DEFAULT'
                                ELSE ' ON DELETE NO ACTION'
            END;

        deferrable_clause := CASE
                                 WHEN rec.condeferrable AND rec.condeferred THEN ' DEFERRABLE INITIALLY DEFERRED'
                                 WHEN rec.condeferrable THEN ' DEFERRABLE INITIALLY IMMEDIATE'
                                 ELSE ' NOT DEFERRABLE'
            END;

        EXECUTE format(
                'ALTER TABLE %I.%I ADD CONSTRAINT %I FOREIGN KEY (%I) REFERENCES %I.fixture_goals(id_uuid)%s%s%s%s',
                rec.child_schema,
                rec.child_table,
                rec.conname,
                rec.child_column,
                current_schema(),
                match_clause,
                on_update_clause,
                on_delete_clause,
                deferrable_clause
                );
    END LOOP;
END
$$;

DO
$$
DECLARE
    pk_name TEXT;
BEGIN
    SELECT c.conname
    INTO pk_name
    FROM pg_constraint c
             JOIN pg_class t ON t.oid = c.conrelid
             JOIN pg_namespace ns ON ns.oid = t.relnamespace
    WHERE c.contype = 'p'
      AND ns.nspname = current_schema()
      AND t.relname = 'fixture_goals';

    IF pk_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE %I.fixture_goals DROP CONSTRAINT %I', current_schema(), pk_name);
    END IF;
END
$$;

ALTER TABLE fixture_goals
    DROP COLUMN id;

ALTER TABLE fixture_goals
    RENAME COLUMN id_uuid TO id;

ALTER TABLE fixture_goals
    ALTER COLUMN id SET NOT NULL,
    ALTER COLUMN id SET DEFAULT uuid_generate_v4();

ALTER TABLE fixture_goals
    ADD CONSTRAINT fixture_goals_pkey PRIMARY KEY (id);

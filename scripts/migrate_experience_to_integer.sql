-- Run on Postgres/Supabase once if `sitter_profiles.experience` is still text/varchar.
-- Skip if the column is already integer.
-- Fix or delete rows that cannot be parsed before running.

ALTER TABLE sitter_profiles
  ALTER COLUMN experience TYPE integer
  USING (
    CASE
      WHEN experience IS NULL OR btrim(experience::text) = '' THEN NULL
      ELSE (
        NULLIF(regexp_replace(btrim(experience::text), '[^0-9]', '', 'g'), '')
      )::integer
    END
  );

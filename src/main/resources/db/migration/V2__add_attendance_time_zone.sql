-- NULL preserves unknown legacy time semantics; do not guess or shift existing rows.
ALTER TABLE attendances
    ADD COLUMN time_zone VARCHAR(32) NULL;

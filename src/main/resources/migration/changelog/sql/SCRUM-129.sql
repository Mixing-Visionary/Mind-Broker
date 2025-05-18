ALTER TABLE processing
    ADD COLUMN IF NOT EXISTS result varchar;

CREATE INDEX IF NOT EXISTS idx_processing_status ON processing USING hash(status);
CREATE INDEX IF NOT EXISTS idx_processing_status_at_result_is_not_null ON processing(status_at) WHERE result IS NOT NULL;
CREATE INDEX IF NOT EXISTS idx_processing_status_at_status_processing ON processing(status_at) WHERE status = 'PROCESSING'::processing_status;
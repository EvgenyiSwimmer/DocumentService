# EXPLAIN

We search by created_at period + status.

Query:
SELECT *
FROM documents
WHERE status = 'SUBMITTED'
AND created_at >= '2025-01-01'
AND created_at <= '2025-12-31'
ORDER BY created_at DESC
LIMIT 20;

EXPLAIN (ANALYZE, BUFFERS):
... (paste output here)

Index:
idx_documents_status_created_at(status, created_at)
Helps PostgreSQL use index/bitmap scan instead of full seq scan for period+status filters.

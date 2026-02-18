# Запрос на выборку документов с определенным статусом, автором и датой создания, 
# с сортировкой по дате и ограничением количества результатов.

EXPLAIN (ANALYZE, BUFFERS)
SELECT id, number, author, status, created_at, updated_at
FROM documents
WHERE status = 'SUBMITTED'
  AND author = 'user1'
  AND created_at BETWEEN '2025-01-01' AND '2025-12-31'
ORDER BY created_at DESC
LIMIT 50;

## EXPLAIN (ANALYZE)

Limit  (cost=0.42..12.85 rows=50 width=64) (actual time=0.041..0.088 rows=12 loops=1)
  ->  Index Scan using idx_documents_status_author_created
      on documents  (cost=0.42..150.35 rows=600 width=64)
      (actual time=0.039..0.082 rows=12 loops=1)
        Index Cond: ((status = 'SUBMITTED')
                     AND (author = 'user1')
                     AND (created_at >= '2025-01-01')
                     AND (created_at <= '2025-12-31'))
Buffers: shared hit=8
Planning Time: 0.110 ms
Execution Time: 0.115 ms

### Что показывает EXPLAIN ANALYZE
Index Scan — используется индекс.
actual time — реальное время выполнения.
rows — сколько строк реально возвращено.
Buffers: shared hit — данные были прочитаны из памяти, а не с диска.
Execution Time — итоговое время выполнения запроса.
Расхождения между плановыми (cost, rows) и фактическими значениями минимальны.
В данном случае запрос выполняется быстро (≈0.1 ms) и использует индекс.

#### Для оптимизации поискового запроса создан составной индекс:

CREATE INDEX idx_documents_status_author_created
ON documents(status, author, created_at);

status — первый фильтр, высокая селективность при поиске по статусу.
author — дополнительная фильтрация.
created_at — используется в диапазоне BETWEEN и сортировке.

Это на дает возможность эффективно фильтровать по статусу и автору, быстро выбирать диапазон дат, 
избежать дополнительной сортировки при ORDER BY created_at.
package ru.itq.documents.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DocumentRepository extends JpaRepository<DocumentEntity, Long> {

    Optional<DocumentEntity> findById(Long id);

    Page<DocumentEntity> findByIdIn(Collection<Long> ids, Pageable pageable);

    @Query("""
           select d from DocumentEntity d
           where (:status is null or d.status = :status)
             and (:author is null or d.author = :author)
             and (:from is null or d.createdAt >= :from)
             and (:to is null or d.createdAt <= :to)
           """)
    Page<DocumentEntity> search(@Param("status") DocumentStatus status,
                               @Param("author") String author,
                               @Param("from") Instant from,
                               @Param("to") Instant to,
                               Pageable pageable);

    @Query("""
           select d.id from DocumentEntity d
           where d.status = :status
           order by d.id asc
           """)
    List<Long> findIdsByStatus(@Param("status") DocumentStatus status, Pageable pageable);

    // ВАЖНО: атомарный переход статуса (конкурентность)
    @Modifying
    @Query("""
           update DocumentEntity d
              set d.status = :to,
                  d.updatedAt = :now
            where d.id = :id
              and d.status = :from
           """)
    int updateStatusIfCurrent(@Param("id") Long id,
                              @Param("from") DocumentStatus from,
                              @Param("to") DocumentStatus to,
                              @Param("now") Instant now);
}


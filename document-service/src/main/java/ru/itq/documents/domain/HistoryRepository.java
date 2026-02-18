package ru.itq.documents.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<HistoryEntity, Long> {

    List<HistoryEntity> findByDocumentIdOrderByCreatedAtAsc(Long documentId);
}


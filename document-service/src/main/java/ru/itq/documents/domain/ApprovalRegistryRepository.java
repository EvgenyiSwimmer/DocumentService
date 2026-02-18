package ru.itq.documents.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ApprovalRegistryRepository extends JpaRepository<ApprovalRegistryEntity, Long> {

    Optional<ApprovalRegistryEntity> findByDocumentId(Long documentId);
}



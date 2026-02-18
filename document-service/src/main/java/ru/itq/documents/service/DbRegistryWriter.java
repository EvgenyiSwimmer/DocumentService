package ru.itq.documents.service;

import org.springframework.stereotype.Component;
import ru.itq.documents.domain.ApprovalRegistryEntity;
import ru.itq.documents.domain.ApprovalRegistryRepository;

import java.time.Instant;

@Component
public class DbRegistryWriter implements RegistryWriter {

    private final ApprovalRegistryRepository registryRepository;

    public DbRegistryWriter(ApprovalRegistryRepository registryRepository) {
        this.registryRepository = registryRepository;
    }

    @Override
    public void writeApproval(Long documentId, Instant approvedAt, String approvedBy) {
        registryRepository.save(ApprovalRegistryEntity.of(documentId, approvedAt, approvedBy));
    }
}

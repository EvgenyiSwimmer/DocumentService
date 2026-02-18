package ru.itq.documents.service;

import java.time.Instant;

public interface RegistryWriter {

    void writeApproval(Long documentId, Instant approvedAt, String approvedBy);
}

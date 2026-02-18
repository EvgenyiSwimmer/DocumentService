package ru.itq.documents.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.itq.documents.domain.*;

import java.time.Instant;

@Service
public class DocumentCommandProcessor {

    private final DocumentRepository documentRepository;
    private final HistoryRepository historyRepository;
    private final RegistryWriter registryWriter;

    public DocumentCommandProcessor(DocumentRepository documentRepository,
                                    HistoryRepository historyRepository,
                                    RegistryWriter registryWriter) {
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
        this.registryWriter = registryWriter;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void submitOneTx(Long id, String initiator, String comment) {
        DocumentEntity d = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document " + id + " not found"));

        if (d.getStatus() != DocumentStatus.DRAFT) {
            throw new ConflictException("Expected DRAFT, actual " + d.getStatus());
        }

        Instant now = Instant.now();
        d.setStatus(DocumentStatus.SUBMITTED);
        d.setUpdatedAt(now);
        documentRepository.save(d);

        historyRepository.save(HistoryEntity.of(d.getId(), initiator, DocumentAction.SUBMIT, comment, now));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void approveOneTx(Long id, String initiator, String comment) {
        if (!documentRepository.existsById(id)) {
            throw new NotFoundException("Document " + id + " not found");
        }

        Instant now = Instant.now();

        int updated = documentRepository.updateStatusIfCurrent(id, DocumentStatus.SUBMITTED, DocumentStatus.APPROVED, now);
        if (updated == 0) {
            DocumentStatus current = documentRepository.findById(id)
                    .map(DocumentEntity::getStatus)
                    .orElseThrow(() -> new NotFoundException("Document " + id + " not found"));
            throw new ConflictException("Expected SUBMITTED, actual " + current);
        }

        try {
            registryWriter.writeApproval(id, now, initiator);
        } catch (Exception ex) {
            throw new RegistryException("Failed to write approval registry for document " + id, ex);
        }

        historyRepository.save(HistoryEntity.of(id, initiator, DocumentAction.APPROVE, comment, now));
    }
}

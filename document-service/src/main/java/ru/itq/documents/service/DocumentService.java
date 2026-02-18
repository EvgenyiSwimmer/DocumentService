package ru.itq.documents.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.itq.documents.api.dto.*;
import ru.itq.documents.domain.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class DocumentService {

    private static final Logger log = LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final HistoryRepository historyRepository;
    private final ApprovalRegistryRepository registryRepository;
    private final DocumentCommandProcessor commandProcessor;

    public DocumentService(DocumentRepository documentRepository,
                           HistoryRepository historyRepository,
                           ApprovalRegistryRepository registryRepository,
                           DocumentCommandProcessor commandProcessor) {
        this.documentRepository = documentRepository;
        this.historyRepository = historyRepository;
        this.registryRepository = registryRepository;
        this.commandProcessor = commandProcessor;
    }

    @Transactional
    public DocumentDto create(CreateDocumentRequest req) {
        Instant now = Instant.now();

        DocumentEntity d = new DocumentEntity();
        d.setAuthor(req.author());
        d.setTitle(req.title());
        d.setStatus(DocumentStatus.DRAFT);
        d.setCreatedAt(now);
        d.setUpdatedAt(now);

        DocumentEntity saved = documentRepository.save(d);
        return DocumentMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public DocumentWithHistoryDto getOne(Long id, boolean withHistory) {
        DocumentEntity d = documentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Document " + id + " not found"));

        if (!withHistory) {
            return new DocumentWithHistoryDto(DocumentMapper.toDto(d), List.of());
        }

        List<HistoryEntity> history = historyRepository.findByDocumentIdOrderByCreatedAtAsc(id);
        return DocumentMapper.toWithHistory(d, history);
    }

    @Transactional(readOnly = true)
    public Page<DocumentDto> getByIds(List<Long> ids, Pageable pageable) {
        return documentRepository.findByIdIn(ids, pageable).map(DocumentMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<DocumentDto> search(DocumentStatus status, String author, Instant from, Instant to, Pageable pageable) {
        return documentRepository.search(status, author, from, to, pageable).map(DocumentMapper::toDto);
    }

    public BatchResultResponse submitBatch(BatchIdsRequest req) {
        List<BatchResultItem> results = new ArrayList<>(req.ids().size());

        for (Long id : req.ids()) {
            results.add(submitOneSafe(id, req.initiator(), req.comment()));
        }

        return new BatchResultResponse(results);
    }

    public BatchResultResponse approveBatch(BatchIdsRequest req) {
        List<BatchResultItem> results = new ArrayList<>(req.ids().size());

        for (Long id : req.ids()) {
            results.add(approveOneSafe(id, req.initiator(), req.comment()));
        }

        return new BatchResultResponse(results);
    }

    public BatchResultItem submitOneSafe(Long id, String initiator, String comment) {
        try {
            commandProcessor.submitOneTx(id, initiator, comment);
            return new BatchResultItem(id, "SUCCESS", null);
        } catch (NotFoundException ex) {
            return new BatchResultItem(id, "NOT_FOUND", "not found");
        } catch (ConflictException ex) {
            return new BatchResultItem(id, "CONFLICT", ex.getMessage());
        }
    }

    public BatchResultItem approveOneSafe(Long id, String initiator, String comment) {
        try {
            commandProcessor.approveOneTx(id, initiator, comment);
            return new BatchResultItem(id, "SUCCESS", null);
        } catch (NotFoundException ex) {
            return new BatchResultItem(id, "NOT_FOUND", "not found");
        } catch (ConflictException ex) {
            return new BatchResultItem(id, "CONFLICT", ex.getMessage());
        } catch (RegistryException ex) {
            return new BatchResultItem(id, "REGISTRY_ERROR", ex.getMessage());
        }
    }

    public ConcurrencyTestResponse concurrencyApproveTest(Long documentId, ConcurrencyTestRequest req) {
        ExecutorService pool = Executors.newFixedThreadPool(req.threads());

        AtomicInteger success = new AtomicInteger();
        AtomicInteger conflict = new AtomicInteger();
        AtomicInteger registryError = new AtomicInteger();
        AtomicInteger notFound = new AtomicInteger();

        List<Callable<Void>> tasks = new ArrayList<>();

        for (int t = 0; t < req.threads(); t++) {
            tasks.add(() -> {
                for (int i = 0; i < req.attempts(); i++) {
                    BatchResultItem r = approveOneSafe(documentId, req.initiator(), "concurrency-test");
                    switch (r.result()) {
                        case "SUCCESS" -> success.incrementAndGet();
                        case "CONFLICT" -> conflict.incrementAndGet();
                        case "NOT_FOUND" -> notFound.incrementAndGet();
                        case "REGISTRY_ERROR" -> registryError.incrementAndGet();
                        default -> conflict.incrementAndGet();
                    }
                }
                return null;
            });
        }

        try {
            long start = System.currentTimeMillis();
            pool.invokeAll(tasks);
            long ms = System.currentTimeMillis() - start;
            log.info("Concurrency test done for docId={}, threads={}, attempts={}, durationMs={}",
                    documentId, req.threads(), req.attempts(), ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            pool.shutdownNow();
        }

        DocumentStatus finalStatus = documentRepository.findById(documentId)
                .map(DocumentEntity::getStatus)
                .orElse(null);

        return new ConcurrencyTestResponse(success.get(), conflict.get(), registryError.get(), notFound.get(), finalStatus);
    }

    @Transactional(readOnly = true)
    public boolean registryExistsFor(Long documentId) {
        return registryRepository.findByDocumentId(documentId).isPresent();
    }
}

package ru.itq.documents.worker;

import org.slf4j.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itq.documents.api.dto.BatchIdsRequest;
import ru.itq.documents.config.WorkerProperties;
import ru.itq.documents.domain.*;
import ru.itq.documents.service.DocumentService;

import java.util.List;

@Component
public class SubmitWorker {

    private static final Logger log = LoggerFactory.getLogger(SubmitWorker.class);

    private final DocumentRepository repo;
    private final DocumentService service;
    private final WorkerProperties props;

    public SubmitWorker(DocumentRepository repo, DocumentService service, WorkerProperties props) {
        this.repo = repo;
        this.service = service;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${app.worker.fixedDelayMs:2000}")
    public void tick() {
        int batchSize = props.getBatchSize();

        List<Long> ids = repo.findIdsByStatus(DocumentStatus.DRAFT, PageRequest.of(0, batchSize));
        if (ids.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        var resp = service.submitBatch(new BatchIdsRequest("submit-worker", "auto-submit", ids));
        long ms = System.currentTimeMillis() - start;

        long success = resp.results().stream().filter(r -> "SUCCESS".equals(r.result())).count();
        log.info("SUBMIT-worker: batchSize={}, picked={}, success={}, durationMs={}", batchSize, ids.size(), success, ms);
    }
}



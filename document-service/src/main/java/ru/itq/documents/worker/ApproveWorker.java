package ru.itq.documents.worker;

import org.slf4j.*;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.itq.documents.api.dto.BatchIdsRequest;
import ru.itq.documents.config.WorkerProperties;
import ru.itq.documents.domain.*;
import ru.itq.documents.service.DocumentService;

import java.util.List;

@Component
@ConditionalOnProperty(prefix = "app.workers", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ApproveWorker {

    private static final Logger log = LoggerFactory.getLogger(ApproveWorker.class);

    private final DocumentRepository repo;
    private final DocumentService service;
    private final WorkerProperties props;

    public ApproveWorker(DocumentRepository repo, DocumentService service, WorkerProperties props) {
        this.repo = repo;
        this.service = service;
        this.props = props;
    }

    @Scheduled(fixedDelayString = "${app.worker.fixedDelayMs:2000}")
    public void tick() {
        int batchSize = props.getBatchSize();

        List<Long> ids = repo.findIdsByStatus(DocumentStatus.SUBMITTED, PageRequest.of(0, batchSize));
        if (ids.isEmpty()) {
            return;
        }

        long start = System.currentTimeMillis();
        var resp = service.approveBatch(new BatchIdsRequest("approve-worker", "auto-approve", ids));
        long ms = System.currentTimeMillis() - start;

        long success = resp.results().stream().filter(r -> "SUCCESS".equals(r.result())).count();
        long registryErr = resp.results().stream().filter(r -> "REGISTRY_ERROR".equals(r.result())).count();

        log.info("APPROVE-worker: batchSize={}, picked={}, success={}, registryErr={}, durationMs={}",
                batchSize, ids.size(), success, registryErr, ms);
    }
}

package ru.itq.documents;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import ru.itq.documents.api.dto.*;
import ru.itq.documents.domain.DocumentAction;
import ru.itq.documents.domain.DocumentStatus;
import ru.itq.documents.service.DocumentService;
import ru.itq.documents.service.DbRegistryWriter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;

@SpringBootTest(properties = "spring.task.scheduling.enabled=true")
class DocumentFlowIT {

    @Autowired
    private DocumentService service;

    @SpyBean
    private DbRegistryWriter registryWriter;

    @Test
    void happyPath_singleDocument() {
        DocumentDto d = service.create(new CreateDocumentRequest("user1", "title1"));
        assertThat(d.status()).isEqualTo(DocumentStatus.DRAFT);

        BatchResultResponse submit = service.submitBatch(new BatchIdsRequest("user1", "submit", List.of(d.id())));
        assertThat(submit.results().get(0).result()).isEqualTo("SUCCESS");

        BatchResultResponse approve = service.approveBatch(new BatchIdsRequest("user2", "approve", List.of(d.id())));
        assertThat(approve.results().get(0).result()).isEqualTo("SUCCESS");

        var withHistory = service.getOne(d.id(), true);
        assertThat(withHistory.document().status()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(withHistory.history()).hasSize(2);
        assertThat(withHistory.history().get(0).action()).isEqualTo(DocumentAction.SUBMIT);
        assertThat(withHistory.history().get(1).action()).isEqualTo(DocumentAction.APPROVE);
        assertThat(service.registryExistsFor(d.id())).isTrue();
    }

    @Test
    void batchSubmit_partial() {
        DocumentDto d1 = service.create(new CreateDocumentRequest("a", "t1"));
        DocumentDto d2 = service.create(new CreateDocumentRequest("a", "t2"));

        service.submitBatch(new BatchIdsRequest("a", null, List.of(d2.id())));

        BatchResultResponse res = service.submitBatch(new BatchIdsRequest("a", null, List.of(d1.id(), d2.id(), 999999L)));
        assertThat(res.results()).hasSize(3);
        assertThat(res.results().get(0).result()).isEqualTo("SUCCESS");
        assertThat(res.results().get(1).result()).isEqualTo("CONFLICT");
        assertThat(res.results().get(2).result()).isEqualTo("NOT_FOUND");
    }

    @Test
    void approve_rollback_when_registry_fails() {
        DocumentDto d = service.create(new CreateDocumentRequest("a", "t"));
        service.submitBatch(new BatchIdsRequest("a", "submit", List.of(d.id())));

        doThrow(new RuntimeException("registry is down"))
                .when(registryWriter)
                .writeApproval(eq(d.id()), any(), anyString());

        BatchResultResponse approve = service.approveBatch(new BatchIdsRequest("b", "approve", List.of(d.id())));
        assertThat(approve.results()).hasSize(1);
        assertThat(approve.results().get(0).result()).isEqualTo("REGISTRY_ERROR");

        var after = service.getOne(d.id(), true);

        assertThat(after.document().status()).isEqualTo(DocumentStatus.SUBMITTED);

        long approveHistoryCount = after.history().stream()
                .filter(h -> h.action() == DocumentAction.APPROVE)
                .count();

        assertThat(approveHistoryCount).isZero();
        assertThat(service.registryExistsFor(d.id())).isFalse();
    }

    @Test
    void approve_concurrency_expectedSingleWinner() {
        DocumentDto d = service.create(new CreateDocumentRequest("a", "t"));
        service.submitBatch(new BatchIdsRequest("a", null, List.of(d.id())));

        ConcurrencyTestResponse resp = service.concurrencyApproveTest(
                d.id(),
                new ConcurrencyTestRequest("tester", 8, 30)
        );

        assertThat(resp.success()).isEqualTo(1);
        assertThat(resp.finalStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(service.registryExistsFor(d.id())).isTrue();
    }
}


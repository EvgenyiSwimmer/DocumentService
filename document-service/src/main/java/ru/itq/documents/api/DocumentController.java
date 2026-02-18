package ru.itq.documents.api;

import jakarta.validation.Valid;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.itq.documents.api.dto.*;
import ru.itq.documents.domain.DocumentStatus;
import ru.itq.documents.service.DocumentService;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    private final DocumentService service;

    public DocumentController(DocumentService service) {
        this.service = service;
    }

    @PostMapping
    public DocumentDto create(@RequestBody @Valid CreateDocumentRequest req) {
        return service.create(req);
    }

    @GetMapping("/{id}")
    public DocumentWithHistoryDto getOne(@PathVariable Long id,
                                         @RequestParam(defaultValue = "false") boolean withHistory) {
        return service.getOne(id, withHistory);
    }

    @GetMapping
    public Page<DocumentDto> getByIds(@RequestParam List<Long> ids,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "20") int size,
                                     @RequestParam(defaultValue = "id,asc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return service.getByIds(ids, pageable);
    }

    @PostMapping("/submit")
    public BatchResultResponse submit(@RequestBody @Valid BatchIdsRequest req) {
        return service.submitBatch(req);
    }

    @PostMapping("/approve")
    public BatchResultResponse approve(@RequestBody @Valid BatchIdsRequest req) {
        return service.approveBatch(req);
    }

    @GetMapping("/search")
    public Page<DocumentDto> search(@RequestParam(required = false) DocumentStatus status,
                                   @RequestParam(required = false) String author,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
                                   @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size,
                                   @RequestParam(defaultValue = "createdAt,desc") String sort) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(parseSort(sort)));
        return service.search(status, author, dateFrom, dateTo, pageable);
    }

    @PostMapping("/{id}/concurrency-approve-test")
    public ConcurrencyTestResponse concurrency(@PathVariable Long id, @RequestBody @Valid ConcurrencyTestRequest req) {
        return service.concurrencyApproveTest(id, req);
    }

    private Sort.Order parseSort(String raw) {
        String[] parts = raw.split(",");
        String field = parts[0].trim();
        Sort.Direction dir = parts.length > 1 ? Sort.Direction.fromString(parts[1].trim()) : Sort.Direction.ASC;
        return new Sort.Order(dir, field);
    }
}


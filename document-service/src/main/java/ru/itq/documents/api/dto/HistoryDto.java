package ru.itq.documents.api.dto;

import ru.itq.documents.domain.DocumentAction;

import java.time.Instant;

public record HistoryDto(
        Long id,
        String actor,
        DocumentAction action,
        String comment,
        Instant createdAt
) { }

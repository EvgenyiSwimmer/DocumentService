package ru.itq.documents.api.dto;

import ru.itq.documents.domain.DocumentStatus;

import java.time.Instant;

public record DocumentDto(
        Long id,
        String number,
        String author,
        String title,
        DocumentStatus status,
        Instant createdAt,
        Instant updatedAt
) { }

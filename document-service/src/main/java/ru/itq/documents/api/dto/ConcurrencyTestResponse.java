package ru.itq.documents.api.dto;

import ru.itq.documents.domain.DocumentStatus;

public record ConcurrencyTestResponse(
        int success,
        int conflict,
        int registryError,
        int notFound,
        DocumentStatus finalStatus
) { }

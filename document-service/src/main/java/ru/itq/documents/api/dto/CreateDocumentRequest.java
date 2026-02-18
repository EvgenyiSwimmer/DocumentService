package ru.itq.documents.api.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateDocumentRequest(
        @NotBlank String author,
        @NotBlank String title
) { }

package ru.itq.documents.api.dto;

public record BatchResultItem(
        Long id,
        String result,
        String message
) { }

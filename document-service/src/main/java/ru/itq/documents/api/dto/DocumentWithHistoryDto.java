package ru.itq.documents.api.dto;

import java.util.List;

public record DocumentWithHistoryDto(
        DocumentDto document,
        List<HistoryDto> history
) { }

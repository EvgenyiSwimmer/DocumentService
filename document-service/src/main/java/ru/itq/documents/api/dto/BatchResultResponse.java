package ru.itq.documents.api.dto;

import java.util.List;

public record BatchResultResponse(List<BatchResultItem> results) { }

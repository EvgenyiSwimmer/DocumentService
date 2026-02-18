package ru.itq.documents.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ConcurrencyTestRequest(
        @NotBlank String initiator,
        @Min(1) @Max(64) int threads,
        @Min(1) @Max(1000) int attempts
) { }

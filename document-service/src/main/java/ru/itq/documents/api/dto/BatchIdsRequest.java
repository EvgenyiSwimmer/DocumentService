package ru.itq.documents.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BatchIdsRequest(
        @NotBlank String initiator,
        String comment,
        @NotEmpty @Size(min = 1, max = 1000) List<Long> ids
) { }

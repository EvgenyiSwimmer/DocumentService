package ru.itq.documents.service;

import ru.itq.documents.api.dto.*;
import ru.itq.documents.domain.*;

import java.util.List;

public class DocumentMapper {

    public static DocumentDto toDto(DocumentEntity d) {
        return new DocumentDto(d.getId(), d.getNumber(), d.getAuthor(), d.getTitle(), d.getStatus(), d.getCreatedAt(), d.getUpdatedAt());
    }

    public static HistoryDto toDto(HistoryEntity h) {
        return new HistoryDto(h.getId(), h.getActor(), h.getAction(), h.getComment(), h.getCreatedAt());
    }

    public static DocumentWithHistoryDto toWithHistory(DocumentEntity d, List<HistoryEntity> history) {
        return new DocumentWithHistoryDto(toDto(d), history.stream().map(DocumentMapper::toDto).toList());
    }
}



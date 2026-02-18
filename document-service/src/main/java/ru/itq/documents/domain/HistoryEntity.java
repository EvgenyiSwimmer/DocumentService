package ru.itq.documents.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "document_history")
public class HistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false)
    private Long documentId;

    @Column(name = "actor", nullable = false)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private DocumentAction action;

    @Column(name = "comment")
    private String comment;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public static HistoryEntity of(Long documentId, String actor, DocumentAction action, String comment, Instant createdAt) {
        HistoryEntity e = new HistoryEntity();
        e.documentId = documentId;
        e.actor = actor;
        e.action = action;
        e.comment = comment;
        e.createdAt = createdAt;
        return e;
    }

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public String getActor() { return actor; }
    public DocumentAction getAction() { return action; }
    public String getComment() { return comment; }
    public Instant getCreatedAt() { return createdAt; }
}


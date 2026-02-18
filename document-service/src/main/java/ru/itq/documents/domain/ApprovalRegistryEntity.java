package ru.itq.documents.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "approval_registry")
public class ApprovalRegistryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "document_id", nullable = false, unique = true)
    private Long documentId;

    @Column(name = "approved_at", nullable = false)
    private Instant approvedAt;

    @Column(name = "approved_by", nullable = false)
    private String approvedBy;

    public static ApprovalRegistryEntity of(Long documentId, Instant approvedAt, String approvedBy) {
        ApprovalRegistryEntity e = new ApprovalRegistryEntity();
        e.documentId = documentId;
        e.approvedAt = approvedAt;
        e.approvedBy = approvedBy;
        return e;
    }

    public Long getId() { return id; }
    public Long getDocumentId() { return documentId; }
    public Instant getApprovedAt() { return approvedAt; }
    public String getApprovedBy() { return approvedBy; }
}


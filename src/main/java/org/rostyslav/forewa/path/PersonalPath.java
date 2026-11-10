package org.rostyslav.forewa.path;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "personal_paths")
public class PersonalPath {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    public UserAccount getUser() {
        return user;
    }

    public void setUser(UserAccount user) {
        this.user = user;
    }

    @Column(nullable = false, length = 120)
    @NotBlank
    @Size(max = 120)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;

    private UUID sourceTemplateId;
    private Integer sourceTemplateRevision;
    @CreationTimestamp
    @Column(nullable = false)
    private Instant createdAt;
    private Instant archivedAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
    @Version
    @Column(nullable = false)
    private long version;

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getSourceTemplateId() {
        return sourceTemplateId;
    }

    public void setSourceTemplateId(UUID sourceTemplateId) {
        this.sourceTemplateId = sourceTemplateId;
    }

    public Integer getSourceTemplateRevision() {
        return sourceTemplateRevision;
    }

    public void setSourceTemplateRevision(Integer sourceTemplateRevision) {
        this.sourceTemplateRevision = sourceTemplateRevision;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archievedAt) {
        this.archivedAt = archievedAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public long getVersion() {
        return version;
    }
}

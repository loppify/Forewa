package org.rostyslav.forewa.skill;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.rostyslav.forewa.path.PersonalPath;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "skill_nodes")
public class SkillNode {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "path_id", nullable = false)
    private PersonalPath path;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    private SkillNode parent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NodeType nodeType;

    @Column(nullable = false, length = 120)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private int sortOrder;
    @Column(nullable = false)
    private Boolean rewardEligible = false;
    @Column(nullable = false)
    private Boolean masteryEnabled = false;
    @Column(nullable = false)
    private short currentMasteryLevel = 0;

    private String sourceTemplateNodeKey;
    private Instant archivedAt;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
    @Version
    private Long version;

    public UUID getId() {
        return id;
    }

    public PersonalPath getPath() {
        return path;
    }

    public void setPath(PersonalPath path) {
        this.path = path;
    }

    public SkillNode getParent() {
        return parent;
    }

    public void setParent(SkillNode parent) {
        this.parent = parent;
    }

    public NodeType getNodeType() {
        return nodeType;
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

    public int getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.sortOrder = sortOrder;
    }

    public Boolean getRewardEligible() {
        return rewardEligible;
    }

    public void setRewardEligible(Boolean rewardEligible) {
        this.rewardEligible = rewardEligible;
    }

    public Boolean getMasteryEnabled() {
        return masteryEnabled;
    }

    public void setMasteryEnabled(Boolean masteryEnabled) {
        this.masteryEnabled = masteryEnabled;
    }

    public short getCurrentMasteryLevel() {
        return currentMasteryLevel;
    }

    public void setCurrentMasteryLevel(short currentMasteryLevel) {
        this.currentMasteryLevel = currentMasteryLevel;
    }

    public String getSourceTemplateNodeKey() {
        return sourceTemplateNodeKey;
    }

    public void setSourceTemplateNodeKey(String sourceTemplateNodeKey) {
        this.sourceTemplateNodeKey = sourceTemplateNodeKey;
    }

    public Instant getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(Instant archivedAt) {
        this.archivedAt = archivedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Long getVersion() {
        return version;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }
}
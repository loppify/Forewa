package org.rostyslav.forewa.auth.UserAccount;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.NonNull;
import org.rostyslav.forewa.auth.Role;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.UUID;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "user_accounts")
public class UserAccount {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String googleSubject;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private Boolean emailVerified;

    private String displayName;
    private String avatarUrl;
    private String timezone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
    @UpdateTimestamp
    @Column(nullable = false)
    private Instant updatedAt;
    @Column(nullable = false)
    private Instant lastLoginAt;
    private Instant deletedAt;
    @Version
    @Column(nullable = false)
    private Long version;


    public @NonNull String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    public @NonNull Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(@NonNull Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public @NonNull Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(@NonNull Instant createdAt) {
        this.createdAt = createdAt;
    }

    public @NonNull Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(@NonNull Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public @NonNull Instant getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(@NonNull Instant lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }

    public Instant getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Instant deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Long getVersion() {
        return version;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getId() {
        return id;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public @NonNull String getGoogleSubject() {
        return googleSubject;
    }

    public void setGoogleSubject(@NonNull String googleSubject) {
        this.googleSubject = googleSubject;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }
}

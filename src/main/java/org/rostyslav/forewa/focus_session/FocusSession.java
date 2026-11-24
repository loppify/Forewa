package org.rostyslav.forewa.focus_session;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.skill.SkillNode;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "focus_sessions")
public class FocusSession {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    UserAccount user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", nullable = false)
    SkillNode skill;

    @Enumerated(EnumType.STRING)
    FocusSessionStatus status;

    @Column(nullable = false)
    Instant startedAt;
    Instant stoppedAt;

    Integer rawElapsedSeconds;
    Integer confirmedSeconds;
    @Column(nullable = false)
    int creditedMinutes = 0;

    @Column(nullable = false)
    boolean rewardEligibleSnapshot;
    @Column(nullable = false)
    LocalDate rewardLocalDate;
    @Column(nullable = false)
    String timezoneSnapshot;

    @Column(columnDefinition = "TEXT")
    String note;
    @Column(nullable = false)
    boolean corrected = false;
    @Column(columnDefinition = "TEXT")
    String correctionNote;

    @CreationTimestamp
    Instant createdAt;
    @UpdateTimestamp
    Instant updatedAt;
    @Version
    Long version;
}

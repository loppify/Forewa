package org.rostyslav.forewa.skill.goal;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record SkillGoalResponse(
        UUID id,
        UUID skillId,
        String title,
        String successCriterion,
        BigDecimal baselineValue,
        BigDecimal targetValue,
        String unit,
        LocalDate targetDate,
        GoalStatus status,
        String outcomeNote,
        Instant startedAt,
        Instant closedAt,
        Instant createdAt,
        Instant updatedAt
) {
}

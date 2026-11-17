package org.rostyslav.forewa.skill.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.rostyslav.forewa.skill.SkillNodeModify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

public record SkillGoalCreate(
        @Size(min = 1, max = 160) @NotBlank String title,
        @NotBlank String successCriterion,
        BigDecimal baselineValue,
        BigDecimal targetValue,
        @Size(max = 40) String unit,
        LocalDate targetDate,
        @NotNull GoalStatus status
) {
}

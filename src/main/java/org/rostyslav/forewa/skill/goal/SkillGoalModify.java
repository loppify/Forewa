package org.rostyslav.forewa.skill.goal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SkillGoalModify(
        @Size(max = 160) @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank") String title,
        @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank") String successCriterion,
        BigDecimal baselineValue,
        BigDecimal targetValue,
        @Size(max = 40) String unit,
        LocalDate targetDate
) {
}

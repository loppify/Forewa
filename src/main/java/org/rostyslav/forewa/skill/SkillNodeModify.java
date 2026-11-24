package org.rostyslav.forewa.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SkillNodeModify(
        @Size(max = 120) @Pattern(regexp = "(?s).*\\S.*", message = "must not be blank") String name,
        String description,
        UUID parentId,
        Integer sortOrder
) {
}

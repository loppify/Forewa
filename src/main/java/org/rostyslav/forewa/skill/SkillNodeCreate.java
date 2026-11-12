package org.rostyslav.forewa.skill;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record SkillNodeCreate(
        @NotBlank @Size(min = 1, max = 120) String name,
        String description,
        @NotNull NodeType nodeType,
        UUID parentId,
        int sortOrder
) {
}

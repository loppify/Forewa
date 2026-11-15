package org.rostyslav.forewa.skill;

import java.util.UUID;

public record SkillNodeResponse(
        UUID id,
        UUID pathId,
        UUID parentId,
        NodeType nodeType,
        String name,
        String description,
        int sortOrder
) {
}

package org.rostyslav.forewa.path;

import java.time.Instant;

public record PersonalPathResponse(
        java.util.UUID id,
        String name,
        String description,
        Instant createdAt
) {
}

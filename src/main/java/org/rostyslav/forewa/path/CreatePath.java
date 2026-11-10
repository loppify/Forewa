package org.rostyslav.forewa.path;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreatePath(
        @NotBlank @Size(min = 1, max = 120) String name,
        @Size(max = 65536) String description
) {
}

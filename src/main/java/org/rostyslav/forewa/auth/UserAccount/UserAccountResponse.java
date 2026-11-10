package org.rostyslav.forewa.auth.UserAccount;

import org.rostyslav.forewa.auth.Role;

import java.util.UUID;

public record UserAccountResponse(
        String displayName,
        String avatarUrl,
        UUID id,
        String email,
        String timezone,
        Role role
) {
}


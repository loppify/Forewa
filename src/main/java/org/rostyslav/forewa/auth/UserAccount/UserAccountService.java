package org.rostyslav.forewa.auth.UserAccount;

import jakarta.transaction.Transactional;
import org.rostyslav.forewa.common.error.AccountDoesNotExistException;
import org.rostyslav.forewa.common.error.IllegalTimeZoneException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Objects;
import java.util.Optional;

@Service
public class UserAccountService {
    private UserAccountRepository userAccountRepository;

    public UserAccountService(UserAccountRepository userAccountRepository) {
        this.userAccountRepository = userAccountRepository;
    }

    @Transactional
    public UserAccount syncGoogleUser(OidcUser user) {
        Optional<UserAccount> userAccount = userAccountRepository.findByGoogleSubject(user.getSubject());
        Instant now = Instant.now();

        if (userAccount.isPresent()) {

            UserAccount account = userAccount.get();
            account.setEmail(Objects.requireNonNull(user.getEmail()));
            account.setAvatarUrl(user.getPicture());
            account.setEmailVerified(Objects.requireNonNull(user.getEmailVerified()));
            account.setDisplayName(user.getFullName());
            account.setLastLoginAt(now);

            return account;
        }

        UserAccount newUserAccount = new UserAccount();
        newUserAccount.setGoogleSubject(Objects.requireNonNull(user.getSubject()));
        newUserAccount.setEmail(Objects.requireNonNull(user.getEmail()));
        newUserAccount.setAvatarUrl(user.getPicture());
        newUserAccount.setEmailVerified(Objects.requireNonNull(user.getEmailVerified()));
        newUserAccount.setDisplayName(user.getFullName());
        newUserAccount.setLastLoginAt(now);
        newUserAccount.setUpdatedAt(now);


        return userAccountRepository.save(newUserAccount);
    }

    public UserAccountResponse getMe(UserAccount user) {
        return new UserAccountResponse(
                user.getDisplayName(),
                user.getAvatarUrl(),
                user.getId(),
                user.getEmail(),
                user.getTimezone(),
                user.getRole()
        );
    }

    @Transactional
    public UserAccountResponse updateMe(UserAccount user, UpdateMeRequest updateMeRequest) {
        UserAccount userAccount = userAccountRepository.findById(user.getId())
                .orElseThrow(AccountDoesNotExistException::new);

        if (updateMeRequest.timezone() != null) {
            if (!ZoneId.getAvailableZoneIds().contains(updateMeRequest.timezone())) {
                throw new IllegalTimeZoneException("Invalid timezone: " + updateMeRequest.timezone());
            }
            userAccount.setTimezone(updateMeRequest.timezone());
        }

        return new UserAccountResponse(
                userAccount.getDisplayName(),
                userAccount.getAvatarUrl(),
                userAccount.getId(),
                userAccount.getEmail(),
                userAccount.getTimezone(),
                userAccount.getRole()
        );
    }
}

package org.rostyslav.forewa.auth.CurrentUser;

import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.auth.UserAccount.UserAccountRepository;
import org.rostyslav.forewa.common.error.AccountDoesNotExistException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Component;

@Component("currentUserResolver")
public class CurrentUserResolver {
    private final UserAccountRepository repository;

    public CurrentUserResolver(UserAccountRepository repository) {
        this.repository = repository;
    }

    public UserAccount resolve(OidcUser principal) {
        return repository.findByGoogleSubject(principal.getSubject())
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(AccountDoesNotExistException::new);
    }
}

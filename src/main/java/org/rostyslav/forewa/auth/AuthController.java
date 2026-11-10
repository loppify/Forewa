package org.rostyslav.forewa.auth;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.CurrentUser.CurrentUser;
import org.rostyslav.forewa.auth.UserAccount.UpdateMeRequest;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.auth.UserAccount.UserAccountResponse;
import org.rostyslav.forewa.auth.UserAccount.UserAccountService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final UserAccountService userAccountService;
    public AuthController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }
    @GetMapping("/me")
    public UserAccountResponse me(@CurrentUser UserAccount user) {
        return userAccountService.getMe(user);
    }
    @PatchMapping("/me")
    public UserAccountResponse updateMe(@CurrentUser UserAccount user,
                                        @RequestBody @Valid UpdateMeRequest  updateMeRequest) {
        return userAccountService.updateMe(
                user,
                updateMeRequest
        );
    }
}

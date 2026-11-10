package org.rostyslav.forewa;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rostyslav.forewa.auth.Role;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.auth.UserAccount.UserAccountRepository;
import org.rostyslav.forewa.auth.UserAccount.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
class Checkpoint1Test {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest");

    @Autowired
    MockMvc mvc;
    @Autowired
    UserAccountService accounts;
    @Autowired
    UserAccountRepository repository;

    private static final String SUB = "google-123";

    private OidcUser googleUser(String email) {
        OidcUser user = mock(OidcUser.class);

        when(user.getSubject()).thenReturn(SUB);
        when(user.getEmail()).thenReturn(email);
        when(user.getEmailVerified()).thenReturn(true);
        when(user.getFullName()).thenReturn("Alex");
        return user;
    }

    @BeforeEach
    void clean() {
        repository.deleteAll();
    }

    @Test
    void firstLoginCreateAccount() {
        UserAccount account =
                accounts.syncGoogleUser(googleUser("alex@gmail.com"));
        assertThat(account.getId()).isNotNull();
        assertThat(account.getRole()).isEqualTo(Role.USER);
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void repeatLoginReusesAccount() {
        UserAccount first = accounts.syncGoogleUser(googleUser("alex@gmail.com"));
        UserAccount second = accounts.syncGoogleUser(googleUser("alex@gmail.com"));

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void changedEmailPreservesIdentity() {
        UserAccount first = accounts.syncGoogleUser(googleUser("alex@gmail.com"));
        UserAccount second = accounts.syncGoogleUser(googleUser("alexxx@gmail.com"));

        assertThat(second.getId()).isEqualTo(first.getId());
        assertThat(second.getEmail()).isEqualTo("alexxx@gmail.com");
        assertThat(repository.count()).isEqualTo(1);
    }

    @Test
    void getMeReturnsLocalAccount() throws Exception {
        UserAccount account =
                accounts.syncGoogleUser(googleUser("alex@gmail.com"));

        mvc.perform(get("/api/v1/me")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(account.getId().toString()))
                .andExpect(jsonPath("$.email")
                        .value("alex@gmail.com"));
    }

    @Test
    void patchMeUpdatesTimezone() throws Exception {
        accounts.syncGoogleUser(googleUser("alex@gmail.com"));

        mvc.perform(patch("/api/v1/me")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timezone":"Europe/Berlin"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.timezone")
                        .value("Europe/Berlin"));

        UserAccount account = repository
                .findByGoogleSubject(SUB)
                .orElseThrow();

        assertThat(account.getTimezone())
                .isEqualTo("Europe/Berlin");
    }

    @Test
    void invalidTimezoneReturns400() throws Exception {
        accounts.syncGoogleUser(googleUser("alex@gmail.com"));

        mvc.perform(patch("/api/v1/me")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timezone":"Mars/Olympus"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code")
                        .value("INVALID_TIMEZONE"));
    }

    @Test
    void patchWithoutCsrfReturns403() throws Exception {
        mvc.perform(patch("/api/v1/me")
                        .with(oidcLogin())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"timezone":"Europe/Berlin"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void unauthenticatedMeIsRejected() throws Exception {
        mvc.perform(get("/api/v1/me")
                        .accept(MediaType.TEXT_HTML))
                .andExpect(status().is3xxRedirection());
    }

}
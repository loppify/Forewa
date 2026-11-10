package org.rostyslav.forewa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.auth.UserAccount.UserAccountRepository;
import org.rostyslav.forewa.auth.UserAccount.UserAccountService;
import org.rostyslav.forewa.path.PathRepository;
import org.rostyslav.forewa.path.PathService;
import org.rostyslav.forewa.path.PersonalPathResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
public class Checkpoint2Test {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest");

    @Autowired
    MockMvc mvc;
    @Autowired
    PathService paths;
    @Autowired
    PathRepository repository;
    @Autowired
    UserAccountService userAccountService;
    @Autowired
    UserAccountRepository userAccountRepository;

    private static final String SUB = "google-123";
    @Autowired
    private ObjectMapper objectMapper;

    private OidcUser googleUser() {
        OidcUser user = mock(OidcUser.class);

        when(user.getSubject()).thenReturn(SUB);
        when(user.getEmail()).thenReturn("alex@gmail.com");
        when(user.getEmailVerified()).thenReturn(true);
        when(user.getFullName()).thenReturn("Alex");
        return user;
    }

    @BeforeEach
    void clean() {
        repository.deleteAll();
        userAccountRepository.deleteAll();
    }

    UserAccount getUser() {
        return userAccountService.syncGoogleUser(googleUser());
    }

    @Test
    void createPath() throws Exception {
        UserAccount user = getUser();
        MvcResult result = mvc.perform(post("/api/v1/paths")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"test path", "description":"test description"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("test path"))
                .andReturn();
        PersonalPathResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                PersonalPathResponse.class
        );

        assertThat(paths.findPersonalPath(response.id(), user));
    }


}

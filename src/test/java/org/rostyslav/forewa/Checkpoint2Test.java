package org.rostyslav.forewa;

import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.auth.UserAccount.UserAccountRepository;
import org.rostyslav.forewa.auth.UserAccount.UserAccountService;
import org.rostyslav.forewa.path.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultMatcher;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
    private static final String SUB2 = "google-123456";
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

    private OidcUser googleUserAdditional() {
        OidcUser user = mock(OidcUser.class);

        when(user.getSubject()).thenReturn(SUB2);
        when(user.getEmail()).thenReturn("steve@gmail.com");
        when(user.getEmailVerified()).thenReturn(true);
        when(user.getFullName()).thenReturn("Steve");
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

    UserAccount getUserAdditional() {
        return userAccountService.syncGoogleUser(googleUserAdditional());
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


    @Test
    void listOwnPaths() throws Exception {
        UserAccount user = getUser();
        paths.createPersonalPath(user, new CreatePath("path1", "description"));
        paths.createPersonalPath(user, new CreatePath("path2", "description"));
        mvc.perform(get("/api/v1/paths")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("path1"))
                .andExpect(jsonPath("$[1].name").value("path2"));
    }

    @Test
    void updateOwnPath() throws Exception {
        UserAccount user = getUser();
        PersonalPathResponse path = paths.createPersonalPath(user, new CreatePath("path1", "description"));

        mvc.perform(patch("/api/v1/paths/%s".formatted(path.id()))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"test path"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("test path"))
                .andExpect(jsonPath("$.description", nullValue()));
    }

    @Test
    void archiveOwnPath() throws Exception {
        UserAccount user = getUser();
        PersonalPathResponse path = paths.createPersonalPath(user, new CreatePath("path1", "description"));

        mvc.perform(post("/api/v1/paths/%s/archive".formatted(path.id()))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isOk());

        mvc.perform(get("/api/v1/paths")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

    }

    @Test
    void profileAbstraction() throws Exception {
        UserAccount user2 = getUserAdditional();
        getUser();
        paths.createPersonalPath(user2, new CreatePath("path1", "description"));
        paths.createPersonalPath(user2, new CreatePath("path2", "description"));


        mvc.perform(get("/api/v1/paths")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB2)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("path1"))
                .andExpect(jsonPath("$[1].name").value("path2"));
        mvc.perform(get("/api/v1/paths")
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}

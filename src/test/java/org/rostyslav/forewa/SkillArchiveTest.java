package org.rostyslav.forewa;

import org.junit.jupiter.api.Test;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.auth.UserAccount.UserAccountService;
import org.rostyslav.forewa.path.PathRepository;
import org.rostyslav.forewa.path.PersonalPath;
import org.rostyslav.forewa.skill.NodeType;
import org.rostyslav.forewa.skill.SkillNode;
import org.rostyslav.forewa.skill.SkillRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Testcontainers
class SkillArchiveTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest");

    @Autowired MockMvc mvc;
    @Autowired UserAccountService accounts;
    @Autowired PathRepository paths;
    @Autowired SkillRepository skills;

    @Test
    void categoryCannotBeArchivedUntilItsActiveChildrenAreArchived() throws Exception {
        OidcUser googleUser = mock(OidcUser.class);
        when(googleUser.getSubject()).thenReturn("archive-user");
        when(googleUser.getEmail()).thenReturn("archive@example.com");
        when(googleUser.getEmailVerified()).thenReturn(true);
        UserAccount user = accounts.syncGoogleUser(googleUser);

        PersonalPath path = new PersonalPath();
        path.setName("Calisthenics");
        path.setUser(user);
        path = paths.save(path);

        SkillNode category = new SkillNode();
        category.setPath(path);
        category.setName("Upper Body");
        category.setNodeType(NodeType.CATEGORY);
        category = skills.save(category);

        SkillNode child = new SkillNode();
        child.setPath(path);
        child.setParent(category);
        child.setName("Pull-ups");
        child.setNodeType(NodeType.TRAINABLE);
        child = skills.save(child);

        mvc.perform(post("/api/v1/skills/{id}/archive", category.getId())
                        .with(oidcLogin().idToken(token -> token.claim("sub", "archive-user")))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_SKILL_HIERARCHY"))
                .andExpect(jsonPath("$.detail").value("Archive child skills first before archiving this category."));

        assertThat(skills.findById(category.getId()).orElseThrow().getArchivedAt()).isNull();

        mvc.perform(post("/api/v1/skills/{id}/archive", child.getId())
                        .with(oidcLogin().idToken(token -> token.claim("sub", "archive-user")))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertThat(skills.findById(child.getId()).orElseThrow().getArchivedAt()).isNotNull();

        mvc.perform(post("/api/v1/skills/{id}/archive", category.getId())
                        .with(oidcLogin().idToken(token -> token.claim("sub", "archive-user")))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertThat(skills.findById(category.getId()).orElseThrow().getArchivedAt()).isNotNull();
    }
}

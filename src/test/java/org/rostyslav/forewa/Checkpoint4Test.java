package org.rostyslav.forewa;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.auth.UserAccount.UserAccountRepository;
import org.rostyslav.forewa.auth.UserAccount.UserAccountService;
import org.rostyslav.forewa.path.PathRepository;
import org.rostyslav.forewa.path.PathService;
import org.rostyslav.forewa.path.PersonalPath;
import org.rostyslav.forewa.skill.NodeType;
import org.rostyslav.forewa.skill.SkillNode;
import org.rostyslav.forewa.skill.SkillRepository;
import org.rostyslav.forewa.skill.goal.GoalRepository;
import org.rostyslav.forewa.skill.goal.GoalStatus;
import org.rostyslav.forewa.skill.goal.SkillGoal;
import org.rostyslav.forewa.skill.goal.SkillGoalResponse;
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

import java.util.List;
import java.util.concurrent.*;

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
public class Checkpoint4Test {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest");

    @Autowired
    MockMvc mvc;
    @Autowired
    PathService pathService;
    @Autowired
    PathRepository pathRepository;
    @Autowired
    UserAccountService userAccountService;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    SkillRepository skillRepository;
    @Autowired
    GoalRepository goalRepository;

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
        goalRepository.deleteAll();
        skillRepository.deleteAll();
        pathRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    UserAccount getUser() {
        return userAccountService.syncGoogleUser(googleUser());
    }

    UserAccount getUserAdditional() {
        return userAccountService.syncGoogleUser(googleUserAdditional());
    }


    @Test
    void createPlannedGoals() throws Exception {
        UserAccount user = getUser();

        PersonalPath path = new PersonalPath();
        path.setName("my path");
        path.setUser(user);
        pathRepository.save(path);

        SkillNode skillNode = new SkillNode();
        skillNode.setName("skill");
        skillNode.setPath(path);
        skillNode.setNodeType(NodeType.TRAINABLE);
        skillRepository.save(skillNode);

        for (int i = 0; i < 5; i++) {
            MvcResult result = mvc.perform(post("/api/v1/skills/%s/goals".formatted(skillNode.getId()))
                            .with(oidcLogin()
                                    .idToken(t -> t.claim("sub", SUB)))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                    {"title":"test %s", "successCriterion":"test criterion %s", "status":"PLANNED"}
                                    """.formatted(i + 1, i + 1)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.title").value("test %s".formatted(i + 1)))
                    .andReturn();
            SkillGoalResponse response = objectMapper.readValue(
                    result.getResponse().getContentAsString(),
                    SkillGoalResponse.class
            );

            assertThat(goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(response.id(), user.getId())
                    .isPresent())
                    .isTrue();
        }

    }

    @Test
    void createActiveGoal() throws Exception {
        UserAccount user = getUser();

        PersonalPath path = new PersonalPath();
        path.setName("my path");
        path.setUser(user);
        pathRepository.save(path);

        SkillNode skillNode = new SkillNode();
        skillNode.setName("skill");
        skillNode.setPath(path);
        skillNode.setNodeType(NodeType.TRAINABLE);
        skillRepository.save(skillNode);

        MvcResult result = mvc.perform(post("/api/v1/skills/%s/goals".formatted(skillNode.getId()))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"test 1", "successCriterion":"test criterion 1", "status":"ACTIVE"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("test 1"))
                .andReturn();
        SkillGoalResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                SkillGoalResponse.class
        );

        assertThat(goalRepository.findAllBySkillNodeId(skillNode.getId())
                .stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .count())
                .isEqualTo(1);

    }

    @Test
    void activateGoal() throws Exception {
        UserAccount user = getUser();

        PersonalPath path = new PersonalPath();
        path.setName("my path");
        path.setUser(user);
        pathRepository.save(path);

        SkillNode skillNode = new SkillNode();
        skillNode.setName("skill");
        skillNode.setPath(path);
        skillNode.setNodeType(NodeType.TRAINABLE);
        skillRepository.save(skillNode);

        SkillGoal skillGoal = new SkillGoal();
        skillGoal.setTitle("goal 1");
        skillGoal.setSuccessCriterion("success");
        skillGoal.setStatus(GoalStatus.PLANNED);
        skillGoal.setSkillNode(skillNode);
        goalRepository.save(skillGoal);

        SkillGoal skillGoal2 = new SkillGoal();
        skillGoal2.setTitle("goal 2");
        skillGoal2.setSuccessCriterion("success");
        skillGoal2.setStatus(GoalStatus.PLANNED);
        skillGoal2.setSkillNode(skillNode);
        goalRepository.save(skillGoal2);

        mvc.perform(post("/api/v1/goals/%s/activate".formatted(skillGoal.getId()))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andReturn();

        assertThat(goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(skillGoal.getId(), user.getId()))
                .hasValueSatisfying(foundGoal ->
                        assertThat(foundGoal.getStatus()).isEqualTo(GoalStatus.ACTIVE));

        mvc.perform(post("/api/v1/goals/%s/activate".formatted(skillGoal2.getId()))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isConflict());

    }

    int activateGoal(SkillGoal skillGoal) throws Exception {
        return mvc.perform(post("/api/v1/goals/%s/activate".formatted(skillGoal.getId()))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andReturn()
                .getResponse()
                .getStatus();
    }

    @Test
    void activate2GoalConcurrently() throws Exception {
        UserAccount user = getUser();

        PersonalPath path = new PersonalPath();
        path.setName("my path");
        path.setUser(user);
        pathRepository.save(path);

        SkillNode skillNode = new SkillNode();
        skillNode.setName("skill");
        skillNode.setPath(path);
        skillNode.setNodeType(NodeType.TRAINABLE);
        skillRepository.save(skillNode);

        SkillGoal skillGoal = new SkillGoal();
        skillGoal.setTitle("goal 1");
        skillGoal.setSuccessCriterion("success");
        skillGoal.setStatus(GoalStatus.PLANNED);
        skillGoal.setSkillNode(skillNode);
        goalRepository.save(skillGoal);

        SkillGoal skillGoal2 = new SkillGoal();
        skillGoal2.setTitle("goal 2");
        skillGoal2.setSuccessCriterion("success");
        skillGoal2.setStatus(GoalStatus.PLANNED);
        skillGoal2.setSkillNode(skillNode);
        goalRepository.save(skillGoal2);

        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try (var executor = Executors.newFixedThreadPool(2)) {

            Callable<Integer> firstTask = () -> {
                ready.countDown();
                start.await();
                return activateGoal(skillGoal);
            };

            Callable<Integer> secondTask = () -> {
                ready.countDown();
                start.await();
                return activateGoal(skillGoal2);
            };

            Future<Integer> first = executor.submit(firstTask);
            Future<Integer> second = executor.submit(secondTask);

            boolean isReady = ready.await(5, TimeUnit.SECONDS);
            assertThat(isReady).isTrue();
            start.countDown();

            int status1 = first.get(10, TimeUnit.SECONDS);
            int status2 = second.get(10, TimeUnit.SECONDS);

            assertThat(List.of(status1, status2))
                    .containsExactlyInAnyOrder(200, 409);
        }

        List<SkillGoal> goals =
                goalRepository.findAllBySkillNodeId(skillNode.getId());

        assertThat(goals.stream()
                .filter(g -> g.getStatus() == GoalStatus.ACTIVE)
                .count())
                .isEqualTo(1);
    }

    @Test
    void testTransitionsHappyPath() throws Exception {
        UserAccount user = getUser();
        PersonalPath path = new PersonalPath();
        path.setName("my path");
        path.setUser(user);
        pathRepository.save(path);

        SkillNode skillNode = new SkillNode();
        skillNode.setName("skill");
        skillNode.setPath(path);
        skillNode.setNodeType(NodeType.TRAINABLE);
        skillRepository.save(skillNode);

        SkillGoal skillGoal1 = createGoal(skillNode, "goal 1", GoalStatus.PLANNED);
        assertGoalTransition(skillGoal1, user, "activate", GoalStatus.ACTIVE);
        assertGoalTransition(skillGoal1, user, "achieve", GoalStatus.ACHIEVED);

        SkillGoal skillGoal2 = createGoal(skillNode, "goal 2", GoalStatus.PLANNED);
        assertGoalTransition(skillGoal2, user, "abandon", GoalStatus.ABANDONED);

        SkillGoal skillGoal3 = createGoal(skillNode, "goal 3", GoalStatus.ACTIVE);
        assertGoalTransition(skillGoal3, user, "replace", GoalStatus.REPLACED);

        SkillGoal skillGoal4 = createGoal(skillNode, "goal 4", GoalStatus.ACTIVE);
        assertGoalTransition(skillGoal4, user, "abandon", GoalStatus.ABANDONED);
    }

    @Test
    void testTransitionsViolation() throws Exception {
        UserAccount user = getUser();
        PersonalPath path = new PersonalPath();
        path.setName("my path");
        path.setUser(user);
        pathRepository.save(path);

        SkillNode skillNode = new SkillNode();
        skillNode.setName("skill");
        skillNode.setPath(path);
        skillNode.setNodeType(NodeType.TRAINABLE);
        skillRepository.save(skillNode);

        SkillGoal skillGoal1 = createGoal(skillNode, "goal 1", GoalStatus.PLANNED);
        assertInvalidTransition(skillGoal1, "achieve");
        assertGoalStatus(skillGoal1, user, GoalStatus.PLANNED);
        assertInvalidTransition(skillGoal1, "replace");
        assertGoalStatus(skillGoal1, user, GoalStatus.PLANNED);

        SkillGoal skillGoal2 = createGoal(skillNode, "goal 2", GoalStatus.ACTIVE);
        assertInvalidTransition(skillGoal2, "activate");
        assertGoalStatus(skillGoal2, user, GoalStatus.ACTIVE);

        SkillGoal skillGoal3 = createGoal(skillNode, "goal 3", GoalStatus.ABANDONED);
        assertInvalidTransitions(skillGoal3);
        assertGoalStatus(skillGoal3, user, GoalStatus.ABANDONED);

        skillGoal3.setStatus(GoalStatus.ACHIEVED);
        skillGoal3 = goalRepository.save(skillGoal3);
        assertInvalidTransitions(skillGoal3);
        assertGoalStatus(skillGoal3, user, GoalStatus.ACHIEVED);

        skillGoal3.setStatus(GoalStatus.REPLACED);
        skillGoal3 = goalRepository.save(skillGoal3);
        assertInvalidTransitions(skillGoal3);
        assertGoalStatus(skillGoal3, user, GoalStatus.REPLACED);
    }

    private SkillGoal createGoal(SkillNode skillNode, String title, GoalStatus status) {
        SkillGoal goal = new SkillGoal();
        goal.setTitle(title);
        goal.setSuccessCriterion("success");
        goal.setStatus(status);
        goal.setSkillNode(skillNode);
        return goalRepository.save(goal);
    }

    private void assertInvalidTransitions(SkillGoal goal) throws Exception {
        for (String transition : List.of("activate", "achieve", "abandon", "replace")) {
            assertInvalidTransition(goal, transition);
        }
    }

    private void assertInvalidTransition(SkillGoal goal, String transition) throws Exception {
        assertInvalidTransition(goal, transition, "INVALID_GOAL_TRANSITION");
    }

    private void assertInvalidTransition(SkillGoal goal, String transition, String errorCode) throws Exception {
        mvc.perform(post("/api/v1/goals/%s/%s".formatted(goal.getId(), transition))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(errorCode));
    }

    private void assertGoalTransition(SkillGoal goal, UserAccount user, String transition,
                                      GoalStatus expectedStatus) throws Exception {
        mvc.perform(post("/api/v1/goals/%s/%s".formatted(goal.getId(), transition))
                        .with(oidcLogin()
                                .idToken(t -> t.claim("sub", SUB)))
                        .with(csrf()))
                .andExpect(status().isOk());
        assertGoalStatus(goal, user, expectedStatus);
    }

    private void assertGoalStatus(SkillGoal goal, UserAccount user, GoalStatus expectedStatus) {
        assertThat(goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(goal.getId(), user.getId()))
                .hasValueSatisfying(foundGoal ->
                        assertThat(foundGoal.getStatus()).isEqualTo(expectedStatus));
    }
}

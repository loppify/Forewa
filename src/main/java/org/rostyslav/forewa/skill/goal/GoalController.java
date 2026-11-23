package org.rostyslav.forewa.skill.goal;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.CurrentUser.CurrentUser;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.common.error.BadRequest;
import org.rostyslav.forewa.common.error.ResourceNotFound;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class GoalController {
    private final GoalService goalService;

    public GoalController(GoalService goalService) {
        this.goalService = goalService;
    }

    @PostMapping("/skills/{skillId}/goals")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillGoalResponse createSkillGoal(@CurrentUser UserAccount user,
                                             @Valid @RequestBody SkillGoalCreate request,
                                             @PathVariable("skillId") UUID skillId) throws BadRequest {
        return goalService.createSkillGoal(request, skillId, user);

    }

    @GetMapping("/skills/{skillId}/goals")
    @ResponseStatus(HttpStatus.OK)
    public List<SkillGoalResponse> getSkillGoals(@CurrentUser UserAccount user,
                                                 @PathVariable("skillId") UUID skillId) throws ResourceNotFound {
        return goalService.getSkillGoals(skillId, user);
    }

    @PostMapping("/goals/{goalId}/activate")
    @ResponseStatus(HttpStatus.OK)
    public void activateGoal(@CurrentUser UserAccount user, @PathVariable("goalId") UUID goalId) throws BadRequest {
        goalService.activateGoal(goalId, user);
    }

    @PostMapping("/goals/{goalId}/achieve")
    @ResponseStatus(HttpStatus.OK)
    public void achieveGoal(@CurrentUser UserAccount user, @PathVariable("goalId") UUID goalId) throws BadRequest {
        goalService.achieveGoal(goalId, user);
    }

    @PostMapping("/goals/{goalId}/replace")
    @ResponseStatus(HttpStatus.OK)
    public void replaceGoal(@CurrentUser UserAccount user, @PathVariable("goalId") UUID goalId) throws BadRequest {
        goalService.replaceGoal(goalId, user);
    }

    @PostMapping("/goals/{goalId}/abandon")
    @ResponseStatus(HttpStatus.OK)
    public void abandonGoal(@CurrentUser UserAccount user, @PathVariable("goalId") UUID goalId) throws BadRequest {
        goalService.abandonGoal(goalId, user);
    }

    @PatchMapping("/goals/{goalId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillGoalResponse modifySkillGoal(@CurrentUser UserAccount user,
                                             @PathVariable("goalId") UUID goalId,
                                             @Valid @RequestBody SkillGoalModify request) throws ResourceNotFound {
        return goalService.modifySkillGoal(goalId, request, user);
    }
}

package org.rostyslav.forewa.skill.goal;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.CurrentUser.CurrentUser;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.common.error.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

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
                                             @PathVariable("skillId") UUID skillId) throws BadRequestException {
        return goalService.createSkillGoal(request, skillId, user);

    }
}

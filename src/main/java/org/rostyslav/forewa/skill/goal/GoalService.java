package org.rostyslav.forewa.skill.goal;

import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class GoalService {
    private final GoalRepository goalRepository;

    public GoalService(GoalRepository goalRepository) {
        this.goalRepository = goalRepository;
    }


    public SkillGoalResponse createSkillGoal(@Valid SkillGoalCreate request,
                                             UUID skillId,
                                             UserAccount user) throws BadRequestException {
        if (request.status() != GoalStatus.PLANNED && request.status() != GoalStatus.ACTIVE) {
            throw new BadRequestException("Invalid goal status");
        }
        SkillGoal activeGoal = goalRepository
                .findByStatusAndSkillNode_Path_User_Id(
                        GoalStatus.ACTIVE,
                        user.getId());

        if (activeGoal != null) {
            throw new BadRequestException("Active goal already exists");
        }

        SkillGoal goal = new SkillGoal();
        goal.setTitle(request.title());
        goal.setSuccessCriterion(request.successCriterion());
        goal.setBaselineValue(request.baselineValue());
        goal.setTargetValue(request.targetValue());
        goal.setUnit(request.unit());
        goal.setTargetDate(request.targetDate());
        goal.setStatus(request.status());

        SkillGoal savedGoal = goalRepository.save(goal);

        return new SkillGoalResponse(
                savedGoal.id,
                savedGoal.skillNode.getId(),
                savedGoal.title,
                savedGoal.successCriterion,
                savedGoal.baselineValue,
                savedGoal.targetValue,
                savedGoal.unit,
                savedGoal.targetDate,
                savedGoal.status,
                savedGoal.outcomeNote,
                savedGoal.startedAt,
                savedGoal.closedAt,
                savedGoal.createdAt,
                savedGoal.updatedAt
        );
    };
}

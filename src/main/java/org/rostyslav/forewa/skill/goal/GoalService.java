package org.rostyslav.forewa.skill.goal;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.common.error.BadRequestException;
import org.rostyslav.forewa.common.error.SkillDoesNotExist;
import org.rostyslav.forewa.skill.SkillNode;
import org.rostyslav.forewa.skill.SkillRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class GoalService {
    private final GoalRepository goalRepository;
    private final SkillRepository skillRepository;

    public GoalService(GoalRepository goalRepository, SkillRepository skillRepository) {
        this.goalRepository = goalRepository;
        this.skillRepository = skillRepository;
    }


    public SkillGoalResponse createSkillGoal(@Valid SkillGoalCreate request,
                                             UUID skillId,
                                             UserAccount user) throws BadRequestException {
        if (request.status() != GoalStatus.PLANNED && request.status() != GoalStatus.ACTIVE) {
            throw new BadRequestException("Invalid goal status");
        }
        SkillGoal activeGoal = goalRepository
                .findByStatusAndSkillNode_Path_User_IdAndSkillNodeId(
                        GoalStatus.ACTIVE,
                        user.getId(),
                        skillId);

        if (activeGoal != null) {
            throw new BadRequestException("Active goal already exists");
        }

        SkillNode skillNode = skillRepository
                .findSkillNodeByIdAndArchivedAtIsNullAndPath_UserId(skillId, user.getId())
                .orElseThrow(SkillDoesNotExist::new);

        SkillGoal goal = new SkillGoal();
        goal.setSkillNode(skillNode);
        goal.setTitle(request.title());
        goal.setSuccessCriterion(request.successCriterion());
        goal.setBaselineValue(request.baselineValue());
        goal.setTargetValue(request.targetValue());
        goal.setUnit(request.unit());
        goal.setTargetDate(request.targetDate());
        goal.setStatus(request.status());

        if (request.status() == GoalStatus.ACTIVE) {
            goal.setStartedAt(Instant.now());
        }

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

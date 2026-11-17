package org.rostyslav.forewa.skill.goal;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.common.error.*;
import org.rostyslav.forewa.skill.NodeType;
import org.rostyslav.forewa.skill.SkillNode;
import org.rostyslav.forewa.skill.SkillRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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

        if (request.status() == GoalStatus.ACTIVE && activeGoal != null) {
            throw new ActiveGoalAlreadyExists("Active goal already exists");
        }

        SkillNode skillNode = skillRepository
                .findSkillNodeByIdAndArchivedAtIsNullAndPath_UserId(skillId, user.getId())
                .orElseThrow(SkillDoesNotExist::new);
        if (skillNode.getNodeType() == NodeType.CATEGORY) {
            throw new SkillIsNotTrainable();
        }
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
    }

    ;

    public List<SkillGoalResponse> getSkillGoals(UUID skillId, UserAccount user) {
        skillRepository.findSkillNodeByIdAndArchivedAtIsNullAndPath_UserId(skillId, user.getId())
                .orElseThrow(SkillDoesNotExist::new);
        List<SkillGoal> skillGoals = goalRepository
                .findAllBySkillNode_IdAndSkillNode_Path_User_IdOrderByStatus(skillId,
                        user.getId());
        List<SkillGoalResponse> skillGoalResponses = new ArrayList<>();
        for (SkillGoal skillGoal : skillGoals) {
            skillGoalResponses.add(new SkillGoalResponse(
                    skillGoal.id,
                    skillGoal.skillNode.getId(),
                    skillGoal.title,
                    skillGoal.successCriterion,
                    skillGoal.baselineValue,
                    skillGoal.targetValue,
                    skillGoal.unit,
                    skillGoal.targetDate,
                    skillGoal.status,
                    skillGoal.outcomeNote,
                    skillGoal.startedAt,
                    skillGoal.closedAt,
                    skillGoal.createdAt,
                    skillGoal.updatedAt
            ));
        }

        return skillGoalResponses;
    }

    public void activateGoal(UUID goalId, UserAccount user) {
        SkillGoal skillGoal = goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(goalId, user.getId())
                .orElseThrow(SkillGoalDoesNotExists::new);
        if (goalRepository.existsSkillGoalByStatusAndSkillNode_Id(GoalStatus.ACTIVE, skillGoal.skillNode.getId())) {
            throw new ActiveGoalAlreadyExists("Active goal already exists");
        }
        if (skillGoal.status != GoalStatus.PLANNED) {
            throw new InvalidGoalTransition("Invalid goal transition");
        }
        skillGoal.setStartedAt(Instant.now());
        skillGoal.status = GoalStatus.ACTIVE;
        goalRepository.save(skillGoal);
    }

    public void achieveGoal(UUID goalId, UserAccount user) {
        SkillGoal skillGoal = goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(goalId, user.getId())
                .orElseThrow(SkillGoalDoesNotExists::new);
        if (skillGoal.status != GoalStatus.ACTIVE) {
            throw new InvalidGoalTransition("Invalid goal transition");
        }
        skillGoal.setClosedAt(Instant.now());
        skillGoal.status = GoalStatus.ACHIEVED;
        goalRepository.save(skillGoal);
    }

    public void replaceGoal(UUID goalId, UserAccount user) {
        SkillGoal skillGoal = goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(goalId, user.getId())
                .orElseThrow(SkillGoalDoesNotExists::new);
        if (skillGoal.status != GoalStatus.ACTIVE) {
            throw new InvalidGoalTransition("Invalid goal transition");
        }
        skillGoal.setClosedAt(Instant.now());
        skillGoal.status = GoalStatus.REPLACED;
        goalRepository.save(skillGoal);
    }

    public void abandonGoal(UUID goalId, UserAccount user) {
        SkillGoal skillGoal = goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(goalId, user.getId())
                .orElseThrow(SkillGoalDoesNotExists::new);
        if (skillGoal.status == GoalStatus.ABANDONED) {
            throw new InvalidGoalTransition("Goal already abandoned");
        }
        if (skillGoal.status != GoalStatus.PLANNED && skillGoal.status != GoalStatus.ACTIVE) {
            throw new InvalidGoalTransition("Invalid goal transition");
        }

        skillGoal.setClosedAt(Instant.now());
        skillGoal.status = GoalStatus.ABANDONED;
        goalRepository.save(skillGoal);
    }

    public SkillGoalResponse modifySkillGoal(UUID goalId, SkillGoalModify request, UserAccount user) {
        SkillGoal skillGoal = goalRepository.findSkillGoalByIdAndSkillNode_Path_User_Id(goalId, user.getId())
                .orElseThrow(SkillGoalDoesNotExists::new);

        if (request.title() != null) {
            skillGoal.setTitle(request.title());
        }
        if (request.successCriterion() != null) {
            skillGoal.setSuccessCriterion(request.successCriterion());
        }
        if (request.baselineValue() != null) {
            skillGoal.setBaselineValue(request.baselineValue());
        }
        if (request.targetValue() != null) {
            skillGoal.setTargetValue(request.targetValue());
        }
        if (request.unit() != null) {
            skillGoal.setUnit(request.unit());
        }
        if (request.targetDate() != null) {
            skillGoal.setTargetDate(request.targetDate());
        }

        skillGoal = goalRepository.save(skillGoal);
        return new SkillGoalResponse(
                skillGoal.id,
                skillGoal.skillNode.getId(),
                skillGoal.title,
                skillGoal.successCriterion,
                skillGoal.baselineValue,
                skillGoal.targetValue,
                skillGoal.unit,
                skillGoal.targetDate,
                skillGoal.status,
                skillGoal.outcomeNote,
                skillGoal.startedAt,
                skillGoal.closedAt,
                skillGoal.createdAt,
                skillGoal.updatedAt
        );
    }
}

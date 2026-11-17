package org.rostyslav.forewa.skill.goal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends CrudRepository<SkillGoal, UUID> {

    SkillGoal findByStatusAndSkillNode_Path_User_IdAndSkillNodeId(GoalStatus status, UUID skillNodePathUserId, UUID skillNodeId);

    List<SkillGoal> findAllBySkillNode_IdAndSkillNode_Path_User_IdOrderByStatus(UUID skillNodeId, UUID userId);

    Optional<SkillGoal> findSkillGoalByIdAndSkillNode_Path_User_Id(UUID id, UUID skillNodePathUserId);

    boolean existsSkillGoalByStatusAndSkillNode_Id(GoalStatus status, UUID skillNodeId);

}

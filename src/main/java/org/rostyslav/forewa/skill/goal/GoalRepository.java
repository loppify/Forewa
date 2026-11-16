package org.rostyslav.forewa.skill.goal;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoalRepository extends CrudRepository<SkillGoal, UUID> {
    SkillGoal findByStatusAndSkillNode_Path_User_Id(GoalStatus status, UUID userId);
}

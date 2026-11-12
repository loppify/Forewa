package org.rostyslav.forewa.skill;

import org.rostyslav.forewa.path.PersonalPath;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SkillRepository extends CrudRepository<SkillNode, UUID> {


    Optional<SkillNode> findSkillNodeByIdAndPathAndArchivedAtIsNull(UUID uuid, PersonalPath path);

    List<SkillNode> findAllByPathAndArchivedAtIsNull(PersonalPath path);

    Optional<SkillNode> findSkillNodeByIdAndArchivedAtIsNullAndPath_UserId(UUID id, UUID path_user_id);


    Optional<SkillNode> findSkillNodeByIdAndArchivedAtIsNullAndPath_UserIdAndPath(UUID id, UUID path_user_id, PersonalPath path);
}

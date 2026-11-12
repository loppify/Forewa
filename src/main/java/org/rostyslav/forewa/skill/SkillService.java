package org.rostyslav.forewa.skill;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.common.error.NodeTypeViolation;
import org.rostyslav.forewa.common.error.PathDoesNotExistException;
import org.rostyslav.forewa.common.error.SkillDoesNotExist;
import org.rostyslav.forewa.path.PathRepository;
import org.rostyslav.forewa.path.PersonalPath;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SkillService {
    private final SkillRepository skillRepository;
    private final PathRepository pathRepository;

    public SkillService(SkillRepository skillRepository, PathRepository pathRepository) {
        this.skillRepository = skillRepository;
        this.pathRepository = pathRepository;
    }

    public SkillNodeResponse createSkill(@Valid SkillNodeCreate skillNodeCreate, UUID pathId, UserAccount user) throws SkillDoesNotExist {
        PersonalPath path = pathRepository.findByIdAndUserId(pathId,
                user.getId()).orElseThrow(PathDoesNotExistException::new);
        SkillNode parentSkill = null;
        if (skillNodeCreate.parentId() != null) {
            parentSkill = skillRepository.findSkillNodeByIdAndPathAndArchivedAtIsNull(skillNodeCreate.parentId(), path).orElseThrow(SkillDoesNotExist::new);

            if (parentSkill.getNodeType().equals(NodeType.TRAINABLE)) {
                throw new NodeTypeViolation();
            }
        }
        SkillNode skillNode = new SkillNode();

        skillNode.setPath(path);
        skillNode.setName(skillNodeCreate.name());
        skillNode.setDescription(skillNodeCreate.description());
        skillNode.setParent(parentSkill);
        skillNode.setSortOrder(skillNodeCreate.sortOrder());
        skillNode.setNodeType(skillNodeCreate.nodeType());
        SkillNode savedSkill = skillRepository.save(skillNode);

        return new SkillNodeResponse(
                savedSkill.getId(),
                savedSkill.getPath().getId(),
                savedSkill.getParent(),
                savedSkill.getNodeType(),
                savedSkill.getName(),
                savedSkill.getDescription(),
                savedSkill.getSortOrder()
        );
    }

    public List<SkillNodeResponse> getPathSkills(UUID pathId, UserAccount user) {
        PersonalPath path = pathRepository
                .findByIdAndUserId(pathId, user.getId())
                .orElseThrow(PathDoesNotExistException::new);
        List<SkillNode> skills = skillRepository.findAllByPathAndArchivedAtIsNull(path);
        List<SkillNodeResponse> responses = new ArrayList<>();
        for (SkillNode skillNode : skills) {
            responses.add(new SkillNodeResponse(
                    skillNode.getId(),
                    skillNode.getPath().getId(),
                    skillNode.getParent(),
                    skillNode.getNodeType(),
                    skillNode.getName(),
                    skillNode.getDescription(),
                    skillNode.getSortOrder()
            ));
        }
        return responses;
    }

    public SkillNodeResponse getSkill(UUID skillId, UserAccount user) {
        SkillNode skillNode = skillRepository
                .findSkillNodeByIdAndArchivedAtIsNullAndPath_UserId(skillId, user.getId())
                .orElseThrow(SkillDoesNotExist::new);
        return new SkillNodeResponse(
                skillNode.getId(),
                skillNode.getPath().getId(),
                skillNode.getParent(),
                skillNode.getNodeType(),
                skillNode.getName(),
                skillNode.getDescription(),
                skillNode.getSortOrder()
        );
    }

    public SkillNodeResponse modifySkill(UUID skillId, SkillNodeModify request, UserAccount user) {
        SkillNode skillNode = skillRepository
                .findSkillNodeByIdAndArchivedAtIsNullAndPath_UserId(skillId, user.getId())
                .orElseThrow(SkillDoesNotExist::new);
        Optional<SkillNode> skillNodeParent = Optional.empty();
        if (skillNode.getParent() != null) {
            skillNodeParent = skillRepository
                    .findSkillNodeByIdAndArchivedAtIsNullAndPath_UserIdAndPath(
                            request.parentId(),
                            user.getId(),
                            skillNode.getPath());

        }

        if (request.name() != null) {
            skillNode.setName(request.name());

        }
        if (request.description() != null) {
            skillNode.setDescription(request.description());
        }

        skillNode.setSortOrder(request.sortOrder());
        if (skillNodeParent.isPresent()) {
            if (skillNodeParent.get().getNodeType().equals(NodeType.TRAINABLE)) {
                throw new NodeTypeViolation();
            }
            if (skillNodeParent.get().equals(skillNode) ||
                    skillNodeParent.get().getSortOrder() > skillNode.getSortOrder()) {
                throw new NodeHierarchyRecursion();
            }
            skillNode.setParent(skillNodeParent.get());

        }
        SkillNode updatedSkill = skillRepository.save(skillNode);

        return new SkillNodeResponse(
                updatedSkill.getId(),
                updatedSkill.getPath().getId(),
                updatedSkill.getParent(),
                updatedSkill.getNodeType(),
                updatedSkill.getName(),
                updatedSkill.getDescription(),
                updatedSkill.getSortOrder()
        );
    }
}

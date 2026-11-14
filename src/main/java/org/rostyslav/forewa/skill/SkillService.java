package org.rostyslav.forewa.skill;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.common.error.NodeHierarchyRecursion;
import org.rostyslav.forewa.common.error.NodeTypeViolation;
import org.rostyslav.forewa.common.error.PathDoesNotExistException;
import org.rostyslav.forewa.common.error.SkillDoesNotExist;
import org.rostyslav.forewa.path.PathRepository;
import org.rostyslav.forewa.path.PersonalPath;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
        SkillNode skillNode = new SkillNode();
        if (skillNodeCreate.parentId() != null) {
            parentSkill = skillRepository.findSkillNodeByIdAndPathAndArchivedAtIsNull(skillNodeCreate.parentId(), path).orElseThrow(SkillDoesNotExist::new);

            if (parentSkill.getNodeType().equals(NodeType.TRAINABLE)) {
                throw new NodeTypeViolation();
            }
            skillNode.setParent(parentSkill);
        }

        skillNode.setPath(path);
        skillNode.setName(skillNodeCreate.name());
        skillNode.setDescription(skillNodeCreate.description());
        if (skillNodeCreate.sortOrder() != null) {
            skillNode.setSortOrder(skillNodeCreate.sortOrder());
        } else if (skillNodeCreate.parentId() != null) {
            skillNode.setSortOrder(
                    skillRepository
                            .findAllByPathAndArchivedAtIsNullAndParent_Id(
                                    path,
                                    skillNodeCreate.parentId()).size()
            );
        } else {
            skillNode.setSortOrder(0);
        }
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

        if (request.parentId() != null) {
            SkillNode skillNodeParent = skillRepository
                    .findSkillNodeByIdAndArchivedAtIsNullAndPath_UserIdAndPath(
                            request.parentId(),
                            user.getId(),
                            skillNode.getPath()).orElseThrow(SkillDoesNotExist::new);
            if (skillNodeParent.getNodeType().equals(NodeType.TRAINABLE)) {
                throw new NodeTypeViolation();
            }
            SkillNode skillNodeParentRecursionCheck = skillNodeParent;
            while (skillNodeParentRecursionCheck != null) {
                if (skillNodeParentRecursionCheck.getId().equals(skillNode.getId())) {
                    throw new NodeHierarchyRecursion();
                }
                skillNodeParentRecursionCheck = skillNodeParentRecursionCheck.getParent();
            }
            skillNode.setParent(skillNodeParent);
        }

        if (request.name() != null) {
            skillNode.setName(request.name());

        }
        if (request.description() != null) {
            skillNode.setDescription(request.description());
        }

        if (request.sortOrder() != null) {
            skillNode.setSortOrder(request.sortOrder());
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

    public void archiveSkill(UUID skillId, UserAccount user) {
        SkillNode skillNode = skillRepository
                .findSkillNodeByIdAndArchivedAtIsNullAndPath_UserId(skillId, user.getId())
                .orElseThrow(SkillDoesNotExist::new);

        skillNode.setArchivedAt(Instant.now());
    }
}

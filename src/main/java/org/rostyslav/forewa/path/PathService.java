package org.rostyslav.forewa.path;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.rostyslav.forewa.common.error.PathDoesNotExistException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class PathService {
    private final PathRepository pathRepository;

    public PathService(PathRepository pathRepository) {
        this.pathRepository = pathRepository;

    }

    public PersonalPathResponse createPersonalPath(UserAccount user, @Valid CreatePath path) {
        PersonalPath personalPath = new PersonalPath();

        personalPath.setUser(user);
        personalPath.setName(path.name());
        personalPath.setDescription(path.description());

        PersonalPath res = pathRepository.save(personalPath);

        return new PersonalPathResponse(
                res.getId(),
                res.getName(),
                res.getDescription(),
                res.getCreatedAt()
        );
    }

    public List<PersonalPathResponse> findAllPaths(UserAccount user) {
        List<PersonalPath> paths = pathRepository.findAllByUserIdAndArchivedAtIsNull(user.getId()).orElse(new ArrayList<>());

        List<PersonalPathResponse> result = new ArrayList<>();
        for (PersonalPath path : paths) {
            result.add(new PersonalPathResponse(
                    path.getId(),
                    path.getName(),
                    path.getDescription(),
                    path.getCreatedAt()
            ));
        }
        return result;
    }

    public PersonalPathResponse findPersonalPath(UUID id, UserAccount user) {
        PersonalPath path = pathRepository.findByIdAndUserId(id, user.getId()).orElseThrow(PathDoesNotExistException::new);

        return new PersonalPathResponse(
                path.getId(),
                path.getName(),
                path.getDescription(),
                path.getCreatedAt()
        );
    }

    public PersonalPathResponse modifyPersonalPath(UUID id, UserAccount user, @Valid CreatePath path) {
        PersonalPath pathToModify = pathRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(PathDoesNotExistException::new);

        pathToModify.setName(path.name());
        pathToModify.setDescription(path.description());
        PersonalPath modifiedPath = pathRepository.save(pathToModify);
        return new PersonalPathResponse(
                modifiedPath.getId(),
                modifiedPath.getName(),
                modifiedPath.getDescription(),
                modifiedPath.getCreatedAt()
        );

    }

    public void archivePersonalPath(UUID id, UserAccount user) {
        PersonalPath pathToModify = pathRepository
                .findByIdAndUserId(id, user.getId())
                .orElseThrow(PathDoesNotExistException::new);

        pathToModify.setArchivedAt(Instant.now());
        pathRepository.save(pathToModify);
    }
}
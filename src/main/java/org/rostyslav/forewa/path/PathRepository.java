package org.rostyslav.forewa.path;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PathRepository extends CrudRepository<PersonalPath, UUID> {


    List<PersonalPath> findAllByUserIdAndArchivedAtIsNull(UUID id);

    Optional<PersonalPath> findByIdAndUserId(UUID pathId, UUID userId);
}

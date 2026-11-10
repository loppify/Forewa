package org.rostyslav.forewa.path;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.CurrentUser.CurrentUser;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/paths")
public class PathController {
    private final PathService pathService;

    public PathController(PathService pathService) {
        this.pathService = pathService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PersonalPathResponse createPersonalPath(@CurrentUser UserAccount user,
            @Valid @RequestBody CreatePath path) {
        return pathService.createPersonalPath(user, path);
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<PersonalPathResponse> getAllPersonalPaths(@CurrentUser UserAccount user) {
        return pathService.findAllPaths(user);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PersonalPathResponse getPersonalPath(@CurrentUser UserAccount user, @PathVariable UUID id) {
        return pathService.findPersonalPath(id, user);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public PersonalPathResponse modifyPersonalPath(@CurrentUser UserAccount user, @PathVariable UUID id, @Valid @RequestBody CreatePath path) {
        return pathService.modifyPersonalPath(id, user, path);
    }

    @PostMapping("/{id}/archive")
    @ResponseStatus(HttpStatus.OK)
    public void archivePersonalPath(@CurrentUser UserAccount user, @PathVariable UUID id) {
        pathService.archivePersonalPath(id, user);
    }

}

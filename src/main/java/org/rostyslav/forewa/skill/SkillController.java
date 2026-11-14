package org.rostyslav.forewa.skill;

import jakarta.validation.Valid;
import org.rostyslav.forewa.auth.CurrentUser.CurrentUser;
import org.rostyslav.forewa.auth.UserAccount.UserAccount;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @PostMapping("/paths/{pathId}/skills")
    @ResponseStatus(HttpStatus.CREATED)
    public SkillNodeResponse addSkill(@Valid @RequestBody SkillNodeCreate skillNodeCreate,
                                      @CurrentUser UserAccount user,
                                      @PathVariable("pathId") UUID pathId) {
        return skillService.createSkill(skillNodeCreate, pathId, user);
    }

    @GetMapping("/paths/{pathId}/skills")
    @ResponseStatus(HttpStatus.OK)
    public List<SkillNodeResponse> getSkills(@CurrentUser UserAccount user,
                                             @PathVariable("pathId") UUID pathId) {
        return skillService.getPathSkills(pathId, user);
    }

    @GetMapping("/skills/{skillId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillNodeResponse getSkill(@CurrentUser UserAccount user,
            @PathVariable("skillId") UUID skillId) {
        return skillService.getSkill(skillId, user);
    }

    @PatchMapping("/skills/{skillId}")
    @ResponseStatus(HttpStatus.OK)
    public SkillNodeResponse modifySkill(@CurrentUser UserAccount user,
                                         @Valid @RequestBody SkillNodeModify request,
                                         @PathVariable("skillId") UUID skillId){
        return skillService.modifySkill(skillId, request, user);
    }

    @PostMapping("/skills/{skillId}")
    @ResponseStatus(HttpStatus.OK)
    public void archiveSkill(@CurrentUser UserAccount user,
                                         @PathVariable("skillId") UUID skillId){
        skillService.archiveSkill(skillId, user);
    }
}

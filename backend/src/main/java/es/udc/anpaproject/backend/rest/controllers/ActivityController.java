/*package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.model.services.ActivityService;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = { "multipart/form-data" })
    public ActivityDto createActivity(
            @RequestPart("data") ActivityDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestAttribute Long userId) throws IOException {

        return activityService.createActivity(dto, userId, image);
    }

    @GetMapping
    public List<ActivityDto> getAll() {
        return activityService.findAll();
    }

    @GetMapping("/{id}")
    public ActivityDto getById(@PathVariable Long id) {
        return activityService.findById(id);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ActivityDto update(@PathVariable Long id, @RequestBody ActivityDto dto, @RequestAttribute Long userId) {
        return activityService.updateActivity(id, dto, userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id, @RequestAttribute Long userId) {
        activityService.deleteActivity(id, userId);
    }
}
*/
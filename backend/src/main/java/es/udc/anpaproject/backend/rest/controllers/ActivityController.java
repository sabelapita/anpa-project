package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.ActivityService;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.rest.dtos.ActivityConverter;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;
import es.udc.anpaproject.backend.rest.dtos.BlockDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@RestController
@RequestMapping("/activities")
public class ActivityController {

    @Autowired
    private ActivityService activityService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = { "multipart/form-data" })
    public ActivityDto createActivity(
            @Valid @RequestPart("data") ActivityDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestAttribute Long userId) throws IOException, PermissionException, InstanceNotFoundException {

        return activityService.createActivity(dto, userId, image);
    }

    @GetMapping
    public BlockDto<ActivityDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) throws InstanceNotFoundException {
        
        Block<Activity> block = activityService.findVisibleActivities(getOptionalUserId(request), page, size);
        
        return new BlockDto<>(
            ActivityConverter.toActivityDtos(block.getItems()),
            block.getExistMoreItems()
        );
    }

    @GetMapping("/{id}")
    public ActivityDto getById(@PathVariable Long id, HttpServletRequest request)
            throws InstanceNotFoundException, PermissionException {
        return activityService.findVisibleById(id, getOptionalUserId(request));
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ActivityDto update(
            @PathVariable Long id, 
            @Valid @RequestBody ActivityDto dto,
            @RequestAttribute Long userId) throws InstanceNotFoundException, PermissionException {
        return activityService.updateActivity(id, dto, userId);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(
            @PathVariable Long id, 
            @RequestAttribute Long userId) throws InstanceNotFoundException, PermissionException {
        activityService.deleteActivity(id, userId);
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ActivityDto createActivityJson(
            @Valid @RequestBody ActivityDto dto,
            @RequestAttribute Long userId)
            throws IOException, PermissionException, InstanceNotFoundException {

        return activityService.createActivity(dto, userId, null);
    }

    private Long getOptionalUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        return userId instanceof Long ? (Long) userId : null;
    }
}

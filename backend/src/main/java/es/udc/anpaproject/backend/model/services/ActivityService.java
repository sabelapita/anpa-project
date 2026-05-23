package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.exceptions.ActivityDeletionException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ActivityService {

    ActivityDto createActivity(ActivityDto dto, Long userId, MultipartFile image)
            throws InstanceNotFoundException, PermissionException, IOException;

    Block<Activity> findActivities(int page, int size);

    Block<Activity> findVisibleActivities(Long userId, String keywords, int page, int size)
        throws InstanceNotFoundException;

    ActivityDto findById(Long id) throws InstanceNotFoundException;

    ActivityDto findVisibleById(Long id, Long userId)
        throws InstanceNotFoundException, PermissionException;

    ActivityDto updateActivity(Long id, ActivityDto dto, Long userId)
            throws InstanceNotFoundException, PermissionException;

    void deleteActivity(Long id, Long userId)
            throws InstanceNotFoundException, PermissionException, ActivityDeletionException;

    static List<ActivityDto> toActivityDtos(List<Activity> activities) {
        return activities.stream()
                .map(es.udc.anpaproject.backend.rest.dtos.ActivityConverter::toActivityDto)
                .toList();
    }
}

package es.udc.anpaproject.backend.rest.dtos;

import es.udc.anpaproject.backend.model.entities.Activity;
import java.util.List;
import java.util.stream.Collectors;

public class ActivityConversor {
    
    private ActivityConversor(){}

    public static ActivityDto toActivityDto(Activity activity) {
        return new ActivityDto(
                activity.getId(),
                activity.getTitle(),
                activity.getDescription(),
                activity.getStartDate(),
                activity.getEndDate(),
                activity.getPrice(),
                activity.getImageUrl(),
                activity.getType()
        );
    }

    public static List<ActivityDto> toActivityDtos(List<Activity> activities) {
        return activities.stream()
                .map(ActivityConversor::toActivityDto)
                .collect(Collectors.toList());
    }

    public static Activity toActivityEntity(ActivityDto dto) {
        Activity activity = new Activity();
        activity.setTitle(dto.getTitle());
        activity.setDescription(dto.getDescription());
        activity.setStartDate(dto.getStartDate());
        activity.setEndDate(dto.getEndDate());
        activity.setPrice(dto.getPrice());
        activity.setImageUrl(dto.getImageUrl());
        activity.setType(dto.getType());
        return activity;
    }
    
    public static void merge(Activity target, ActivityDto dto) {
        target.setTitle(dto.getTitle());
        target.setDescription(dto.getDescription());
        target.setStartDate(dto.getStartDate());
        target.setEndDate(dto.getEndDate());
        target.setPrice(dto.getPrice());
        target.setImageUrl(dto.getImageUrl());
        target.setType(dto.getType());
    }

}

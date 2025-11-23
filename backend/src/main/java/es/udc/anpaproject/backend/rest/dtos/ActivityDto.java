package es.udc.anpaproject.backend.rest.dtos;

import java.time.LocalDate;

import es.udc.anpaproject.backend.model.entities.ActivityType;

public class ActivityDto {
    private Long id;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double price;
    private String imageUrl;
    private ActivityType type;

    public ActivityDto(){}

    public ActivityDto(Long id, String title, String description, LocalDate startDate, LocalDate endDate,
                       Double price, String imageUrl, ActivityType type) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.price = price;
        this.imageUrl = imageUrl;
        this.type = type;
    }
    
    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public ActivityType getType() { return type; }
    public void setType(ActivityType type) { this.type = type; }
}

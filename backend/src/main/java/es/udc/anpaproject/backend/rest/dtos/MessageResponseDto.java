package es.udc.anpaproject.backend.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MessageResponseDto {

    private String response;

    public MessageResponseDto() {}

    public MessageResponseDto(String response) {
        this.response = response != null ? response.trim() : null;
    }

    @NotNull
    @Size(min = 1, max = 5000)
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response != null ? response.trim() : null;
    }
}
package es.udc.anpaproject.backend.rest.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ForgotPasswordParamsDto {

    private String email;

    public ForgotPasswordParamsDto() {}

    @NotNull
    @Size(min = 1, max = 60)
    @Email
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email != null ? email.trim() : null;
    }
}

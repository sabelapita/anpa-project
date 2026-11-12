package es.udc.anpaproject.backend.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ResetPasswordParamsDto {

    private String token;
    private String newPassword;

    public ResetPasswordParamsDto() {}

    @NotNull
    @Size(min = 1, max = 200)
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token != null ? token.trim() : null;
    }

    @NotNull
    @Size(min = 1, max = 60)
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}

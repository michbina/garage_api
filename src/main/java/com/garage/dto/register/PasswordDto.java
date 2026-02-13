package com.garage.dto.register;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordDto {

	@NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
        regexp = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d).*$",
        message = "Le mot de passe doit contenir une majuscule, une minuscule et un chiffre"
    )
    private String password;

    @NotBlank(message = "La confirmation est obligatoire")
    private String confirmPassword;
    
    @AssertTrue(message = "Les mots de passe ne correspondent pas")
    public boolean isPasswordsMatching() {
        if (password == null || confirmPassword == null) {
            return false;
        }
        return password.equals(confirmPassword);
    }

}


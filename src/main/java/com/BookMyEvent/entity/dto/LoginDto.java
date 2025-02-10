package com.BookMyEvent.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginDto {
    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Неправильний формат email")
    private String email;

    @NotBlank(message = "Пароль не може бути порожнім")
    @Size(min = 8, message = "Пароль повинен містити щонайменше 8 символів")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).+$")
    private String password;
}

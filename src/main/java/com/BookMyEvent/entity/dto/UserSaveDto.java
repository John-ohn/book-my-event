package com.BookMyEvent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserSaveDto {

    @NotBlank(message = "Ім'я користувача не може бути порожнім")
    @Pattern(regexp = "^[\\p{L} ]{3,40}", message = "Write a correct First Name. Use only chars. Min 3 not more than 40.")
    private String name;

    @NotBlank(message = "Email не може бути порожнім")
    @Email(message = "Неправильний формат email")
    private String email;

    @NotBlank(message = "Пароль не може бути порожнім")
    @Size(min = 8, message = "Пароль повинен містити щонайменше 8 символів")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).+$",
            message = "Пароль повинен містити велику літеру, цифру і спеціальний символ")
    private String password;

    @Pattern(regexp = "^\\d{10,15}$", message = "Номер телефону має містити від 10 до 15 цифр")
    private String phone;
}

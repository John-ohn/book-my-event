package com.BookMyEvent.entity.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateDto {

  @Pattern(regexp = "^[\\p{L} ]{2,50}", message = "Write a correct First Name. Use only chars. Min 3 not more than 30.")
  private String name;
  @Pattern(regexp = "^[\\p{L} ]{2,50}", message = "Write a correct sur name. Use only chars. Min 3 not more than 30.")
  private String surname;
  @Email(message = "Incorrect email format")
  private String email;
  private LocalDate birthdayDate;
  private String location;
  @Pattern(regexp = "^\\+?[0-9]{10,12}$", message = "Write a correct phone. Use only number and +. Min 10 not more than 13.")
  private String phoneNumber;
}

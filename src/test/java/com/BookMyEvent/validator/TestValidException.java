package com.BookMyEvent.validator;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.UserSaveDto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;

import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


import java.time.LocalDateTime;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestValidException {

  private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

  @BeforeEach
  void setUp() {

    System.out.println("Validator initialized: " + validator);
  }

  @Value("${front.url}")
  private String frontUrl;

  private LoginDto loginDto;

  private UserSaveDto userSaveDto;

  private User userOne;

  private final String email = "sewewt@code.com";
  private final String password = "As123ertyuer";

  @BeforeEach
  public void initSetUp() {
    loginDto = LoginDto.builder()
        .email(email)
        .password(password)
        .build();

    userSaveDto = UserSaveDto.builder()
        .name("Ronald")
        .email(email)
        .password(password)
        .build();

    userOne = User.builder()
        .id(new ObjectId("66c648b600179737a3d5c235"))
        .name("Ronald")
        .email(email)
        .password(password)
        .location("Kyiv")
        .mailConfirmation(true)
        .status(Status.ACTIVE)
        .role(Role.VISITOR)
        .creationDate(LocalDateTime.now())
        .build();
  }

  @Test
  void testValidLoginDto() {
    LoginDto dto = LoginDto.builder()
        .email("example@test.com")
        .password("Strong@123")
        .build();

    Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);

    assertTrue(violations.isEmpty(), "Should have no validation errors");
  }

  @Test
  void testInvalidEmail() {
    LoginDto dto = LoginDto.builder()
        .email("invalid-email")
        .password("Strong@123")
        .build();

    Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);

    assertEquals(1, violations.size());
    assertEquals("Неправильний формат email", violations.iterator().next().getMessage());
  }

  @Test
  void testEmptyPassword() {
    LoginDto dto = LoginDto.builder()
        .email("example@test.com")
        .password("")
        .build();

    Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);

    assertEquals(3, violations.size()); // @NotBlank і @Pattern повинні спрацювати
  }

  @Test
  void testInvalidPasswordPattern() {
    LoginDto dto = LoginDto.builder()
        .email("example@test.com")
        .password("weakpassword")
        .build();

    Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);

    assertEquals(1, violations.size());
    assertEquals("має відповідати шаблону \"^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).+$\"",
        violations.iterator().next().getMessage());
  }
}

package com.BookMyEvent.controller;

import com.BookMyEvent.dao.MailConfirmationRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.UserEmailData;
import com.BookMyEvent.entity.dto.EmailVerificationResponseDTO;
import com.BookMyEvent.entity.dto.LoginDto;
import com.BookMyEvent.entity.dto.LoginResponse;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.AuthService;

import com.BookMyEvent.service.DeletedUsersService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.is;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:integrationtest.properties")
@Slf4j
class AuthControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Mock
  private AuthService authService;

  @MockBean
  private UserService userService;

  @MockBean
  private UserRepository repository;

  @MockBean
  private DeletedUsersService deletedUsersService;

  @MockBean
  private PasswordEncoder passwordEncoder;

  @MockBean
  private MailConfirmationRepository mailRepository;

  @MockBean
  private MailService mailService;

  @Value("${front.url}")
  private String frontUrl;


  @BeforeEach
  public void setup() {
    objectMapper.registerModule(new JavaTimeModule());
  }

  private LoginDto loginDto;

  private UserSaveDto userSaveDto;

  private User userOne;

  private final String email = "sewewt@code.com";
  private final String password = "As123ertyuer!";

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

  @Nested
  @DisplayName("Test AuthController method Registration.")
  class Registration {
    @Test
    @DisplayName("Test AuthController method Registration Positive Scenario")
    void testMethodRegistrationPositiveScenario() throws Exception {

      String successMessage = "User registered successfully.";
      String requestBody = objectMapper.writeValueAsString(userSaveDto);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.empty());
      doNothing().when(mailService).sendHtmlEmailAfterRegistration(email);
      when(repository.save(any(User.class))).thenReturn(userOne);
      doNothing().when(mailService).deleteOldEmails(email);

      mockMvc.perform(post("/authorize/registration")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isCreated())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(successMessage)));
    }

    @Test
    @DisplayName("Test AuthController method Registration. Negative Scenario Email is On Deleted List.")
    void testRegistrationNegativeScenarioIsOnDeletedList() throws Exception {
      String message = String.format("The email (%s) has been deleted and is no longer accessible.", email);

      String requestBody = objectMapper.writeValueAsString(userSaveDto);

      when(deletedUsersService.emailExist(userSaveDto.getEmail())).thenReturn(true);

      mockMvc.perform(post("/authorize/registration")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    @DisplayName("Test AuthController method Registration. Negative Scenario Email is already in use.")
    void testRegistrationNegativeScenarioIsAlreadyInUse() throws Exception {
      String message = "Email is already in use.";

      String requestBody = objectMapper.writeValueAsString(userSaveDto);
      when(deletedUsersService.emailExist(userSaveDto.getEmail())).thenReturn(false);
      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));

      mockMvc.perform(post("/authorize/registration")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));
    }

    @Test
    @DisplayName("Test AuthController method Registration. Negative Scenario Email already exists and needs confirmation.")
    void testRegistrationNegativeScenarioAlreadyExistsAndNeedsConfirmation() throws Exception {
      String message = String.format("This email address (%s) already exists and needs confirmation.",
          userSaveDto.getEmail());

      String requestBody = objectMapper.writeValueAsString(userSaveDto);
      userOne.setMailConfirmation(false);
      log.info("userOne {}", userOne);
      when(deletedUsersService.emailExist(userSaveDto.getEmail())).thenReturn(false);
      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));

      mockMvc.perform(post("/authorize/registration")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));
    }


  }

  @Nested
  @DisplayName("Test AuthController method EmailVerificationCheck.")
  class EmailVerificationCheck {

    @Test
    @DisplayName("Test AuthController method emailVerificationCheck Positive Scenario.")
    void testMethodEmailVerificationCheckPositiveScenario() throws Exception {

      String message = String.format("Email (%s) is confirmed", email);
      String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
      UserEmailData userEmailData = new UserEmailData(userOne.getId().toHexString(), userOne.getEmail(), "password");
      User mockUser = new User();

      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));
      when(mailRepository.findByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userEmailData));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
      when(repository.save(any(User.class))).thenReturn(mockUser);
      doNothing().when(mailRepository).delete(any(UserEmailData.class));

      mockMvc.perform(get("/authorize/mail-confirmation/{email}/{password}", email, "password")
          )
          .andExpect(status().isFound())
          .andExpect(header().string("Location", frontUrl + "/?emailConfirmed=true&message=" + encodedMessage + "&email=" + email)
          );
    }

    @Test
    @DisplayName("Test AuthController method emailVerificationCheck. Negative Scenario User not found.")
    void testMethodEmailVerificationCheckNegativeScenarioUserNotFound() throws Exception {
      String message = String.format("User not found email (%s).", email);

      when(repository.findUserByEmail(email)).thenReturn(Optional.empty());

      mockMvc.perform(get("/authorize/mail-confirmation/{email}/{password}", email, "password")
          )
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value()))
          );
    }

    @Test
    @DisplayName("Test AuthController method emailVerificationCheck. Negative Scenario Wrong Password")
    void testMethodEmailVerificationCheckNegativeScenarioWrongPassword() throws Exception {

      String message = "Wrong password";
      String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
      UserEmailData userEmailData = new UserEmailData(userOne.getId().toHexString(), userOne.getEmail(), "password");
      String redirectUrl = frontUrl + "/?emailConfirmed=true&message=" + encodedMessage + "&email=" + email;
      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));
      when(mailRepository.findByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userEmailData));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      log.info("testMethodEmailVerificationCheckNegativeScenarioWrongPassword - redirectUrl: " + redirectUrl);

      mockMvc.perform(get("/authorize/mail-confirmation/{email}/{password}", email, "password")
          )
          .andExpect(status().isFound())
          .andExpect(header().string("Location", redirectUrl)
          );
    }

    @Test
    @DisplayName("Test AuthController method emailVerificationCheck. Negative Scenario Email not found (MailConfirmationRepository)")
    void testMethodEmailVerificationCheckNegativeScenarioEmailNotFound() throws Exception {

      String message = String.format("Email (%s) not found", email);
      String encodedMessage = URLEncoder.encode(message, StandardCharsets.UTF_8);
      String redirectUrl = frontUrl + "/?emailConfirmed=true&message=" + encodedMessage + "&email=" + email;
      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));
      when(mailRepository.findByEmail(userSaveDto.getEmail())).thenReturn(Optional.empty());

      log.info("testMethodEmailVerificationCheckNegativeScenarioWrongPassword - redirectUrl: " + redirectUrl);

      mockMvc.perform(get("/authorize/mail-confirmation/{email}/{password}", email, "password")
          )
          .andExpect(status().isFound())
          .andExpect(header().string("Location", redirectUrl));
    }
  }

  @Nested
  @DisplayName("Test AuthController method Login.")
  class Login {
    @Test
    @DisplayName("Test AuthController method Login Positive Scenario.")
    void testLogin() throws Exception {
      LoginResponse loginResponse = new LoginResponse(
          "66c648b600179737a3d5c235",
          "Ronald",
          "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbm9ueW1vdXNVc2VyIiwicm" +
              "9sZSI6IkFETUlOIiwiaWF0IjoxNzI4NzM2MzcwLCJleHAiOjE3Mjg3NzIzNzB9.sQs" +
              "6sV6GhkBXLkRs5JyiOW7SN0YdfFpyu7HrRp7x8PFFyq_biyHut4eTKynzMSbVwQCDRqFlL_b88RsverSmBA",
          String.format("Email (%s) is confirmed",
              loginDto.getEmail()),
          200
      );
      String requestBody = objectMapper.writeValueAsString(loginDto);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.userId", is(loginResponse.getUserId())));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Email is On Deleted List.")
    void testLoginNegativeScenarioIsOnDeletedList() throws Exception {
      String message = String.format("The email (%s) has been deleted and is no longer accessible.", email);
      String requestBody = objectMapper.writeValueAsString(loginDto);

      when(deletedUsersService.emailExist(email)).thenReturn(true);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Email is not registered.")
    void testLoginNegativeScenarioIsNotRegistered() throws Exception {
      String message = String.format("Email (%s) is not registered.", loginDto.getEmail());
      String requestBody = objectMapper.writeValueAsString(loginDto);

      when(authService.login(loginDto)).thenThrow(new GeneralException(message, HttpStatus.BAD_REQUEST));

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Confirm your email.")
    void testLoginNegativeScenarioConfirmYourEmail() throws Exception {
      String message = String.format("Confirm your email (%s)", loginDto.getEmail());

      String requestBody = objectMapper.writeValueAsString(loginDto);
      userOne.setMailConfirmation(false);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));


      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.UNAUTHORIZED.value())));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario User banned.")
    void testLoginNegativeScenarioUserBanned() throws Exception {
      String message = "User banned.";

      String requestBody = objectMapper.writeValueAsString(loginDto);

      userOne.setStatus(Status.BANNED);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Wrong password.")
    void testLoginNegativeScenarioWrongPassword() throws Exception {
      String message = "Wrong password";

      String requestBody = objectMapper.writeValueAsString(loginDto);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value())));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario (Validated) Invalid Format Email.")
    void testLoginScenarioValidatedInvalidFormatEmail() throws Exception {

      LoginDto invalidLoginDto = LoginDto.builder()
          .email("invalid-email")
          .password("Password1@")
          .build();
      String requestBody = objectMapper.writeValueAsString(invalidLoginDto);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details.email").value("Неправильний формат email"));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario (Validated) Email Not Blank.")
    void testLoginScenarioValidatedEmailNotBlank() throws Exception {

      LoginDto invalidLoginDto = LoginDto.builder()
//          .email("")
          .password("Password1@")
          .build();
      String requestBody = objectMapper.writeValueAsString(invalidLoginDto);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details.email").value("Email не може бути порожнім"));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario (Validated) Invalid Format Password.")
    void testLoginScenarioValidatedInvalidFormatPassword() throws Exception {

      LoginDto invalidLoginDto = LoginDto.builder()
          .email(email)
          .password("Password1")
          .build();
      String requestBody = objectMapper.writeValueAsString(invalidLoginDto);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details.password").value("must match \"^(?=.*[a-z])(?=.*[A-Z])(?=.*[0-9])(?=.*[@$!%*?&]).+$\""));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario (Validated) Invalid Password Not Blank.")
    void testLoginScenarioValidatedInvalidPasswordNotBlank() throws Exception {

      LoginDto invalidLoginDto = LoginDto.builder()
          .email(email)

          .build();
      String requestBody = objectMapper.writeValueAsString(invalidLoginDto);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details.password").value("Пароль не може бути порожнім"));
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario (Validated) Invalid Password Min Size.")
    void testLoginScenarioValidatedInvalidPasswordMinSize() throws Exception {

      LoginDto invalidLoginDto = LoginDto.builder()
          .email(email)
          .password("Pa1!")
          .build();
      String requestBody = objectMapper.writeValueAsString(invalidLoginDto);

      mockMvc.perform(post("/authorize/login")
              .contentType(MediaType.APPLICATION_JSON)
              .content(requestBody))
          .andExpect(status().isBadRequest())
          .andExpect(jsonPath("$.details.password").value("Пароль повинен містити щонайменше 8 символів"));
    }

  }

  @Nested
  @DisplayName("Test AuthController method CheckExistEmailAndIsAccessible.")
  class CheckExistEmailAndIsAccessible {
    @Test
    @DisplayName("Test AuthController method CheckExistEmailAndIsAccessible Positive Scenario.")
    void checkExistEmailAndIsAccessibleScenarioIsNotUse() throws Exception {
      String message = String.format("Email (%s) is not use.", email);

      EmailVerificationResponseDTO response = new EmailVerificationResponseDTO(
          HttpStatus.OK.value(), message, false);

      when(authService.checkExistEmailAndIsAccessible(email)).thenReturn(response);

      mockMvc.perform(get("/authorize/exist/sewewt@code.com"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(response.getStatusCode())))
          .andExpect(jsonPath("$.emailExist", is(response.getEmailExist())));
    }

    @Test
    @DisplayName("Test AuthController method CheckExistEmailAndIsAccessible. Scenario Email already in use return.")
    void checkExistEmailAndIsAccessibleScenarioAlreadyInUse() throws Exception {
      String message = String.format("Email (%s) is already in use.", email);

      EmailVerificationResponseDTO response = new EmailVerificationResponseDTO(
          HttpStatus.OK.value(), message, true);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.existsByEmail(email)).thenReturn(true);

      mockMvc.perform(get("/authorize/exist/" + email))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(response.getStatusCode())))
          .andExpect(jsonPath("$.emailExist", is(response.getEmailExist())));
    }

    @Test
    @DisplayName("Test AuthController method CheckExistEmailAndIsAccessible. Negative Scenario Email is On Deleted List.")
    void checkExistEmailAndIsAccessibleScenarioIsOnDeletedList() throws Exception {
      String message = String.format("The email (%s) has been deleted and is no longer accessible.", email);

      when(deletedUsersService.emailExist(email)).thenReturn(true);

      mockMvc.perform(get("/authorize/exist/sewewt@code.com"))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.FORBIDDEN.value())));
    }
  }

  @Nested
  @DisplayName("Test AuthController method SendLetterToUser.")
  class sendLetterToUser{

    @Test
    @DisplayName("Test AuthController method SendLetterToUser. Positive Scenario containsMessage is true.")
    void SendLetterToUserScenarioContainsMessageIsTrue() throws Exception {
      String message = "The user has such a letter in their correspondence.";

      when(mailService.getMessagesFromUser(email)).thenReturn(true);

      mockMvc.perform(get("/authorize/sendLetterToUser/{email}",email))
          .andExpect(status().isCreated())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.CREATED.value())));
    }

    @Test
    @DisplayName("Test AuthController method SendLetterToUser. Positive Scenario containsMessage is false.")
    void SendLetterToUserScenarioContainsMessageIsFalse() throws Exception {
      String message = "The message was sent to the user again.";

      when(mailService.getMessagesFromUser(email)).thenReturn(false);

      mockMvc.perform(get("/authorize/sendLetterToUser/{email}",email))
          .andExpect(status().isCreated())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(message)))
          .andExpect(jsonPath("$.status", is(HttpStatus.CREATED.value())));
    }
  }

}
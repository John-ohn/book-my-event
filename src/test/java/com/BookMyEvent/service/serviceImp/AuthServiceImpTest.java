package com.BookMyEvent.service.serviceImp;

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
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.DeletedUsersService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@Slf4j
class AuthServiceImpTest {

  @InjectMocks
  private AuthServiceImp authService;

  @Mock
  private UserService userService;

  @Mock
  private UserRepository repository;

  @Mock
  private DeletedUsersService deletedUsersService;

  @Mock
  private PasswordEncoder passwordEncoder;

  @Mock
  private MailConfirmationRepository mailRepository;

  @Mock
  private MailService mailService;

  @Mock
  private UserMapper userMapper;

  @Mock
  private JwtAuthentication jwtAuthentication;

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

  @ParameterizedTest
  @ValueSource(strings = {
      "mySecretPassword", "mySecretPa", "password3!", "pass@SSord4", "password5",
      "ordQQWW23446", "Bdssjjeei123&", "Saddlkdjjxnb,d", "passpassorw", "123456789"
  })
  @DisplayName("Test BCryptPasswordEncoder Scenario Different Hashes For Same Password.")
  void testDifferentHashesForSamePassword(String correctPassword) {
    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);
    String wrongPassword = "myWrongPassword";

    String hash1 = passwordEncoder.encode(correctPassword);
    String hash2 = passwordEncoder.encode(correctPassword);

    log.info("hash1.hashCode()- {}. hash2.hashCode() - {} .", hash1.hashCode(), hash2.hashCode());
    assertNotEquals(hash1.hashCode(), hash2.hashCode(), "Hashes should be different due to random salt generation");
    log.info("hash1- {}. hash2 - {} .", hash1, hash2);
    assertNotEquals(hash1, hash2, "Hashes should be different due to random salt generation");

    assertTrue(passwordEncoder.matches(correctPassword, hash1));
    assertTrue(passwordEncoder.matches(correctPassword, hash2));

    assertFalse(passwordEncoder.matches(wrongPassword, hash1));
    assertFalse(passwordEncoder.matches(wrongPassword, hash2));
  }

  @Nested
  @DisplayName("Test AuthServiceImp method Registration.")
  class Registration {
    @Test
    @DisplayName("Test AuthServiceImp method Registration Positive Scenario")
    void testMethodRegistrationPositiveScenario() {
      String successMessage = "User registered successfully.";

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.empty());
      doNothing().when(mailService).sendHtmlEmailAfterRegistration(email);
      when(userMapper.toUserFromUserSaveDto(userSaveDto)).thenReturn(userOne);
      when(repository.save(any(User.class))).thenReturn(userOne);
      doNothing().when(mailService).deleteOldEmails(email);

      String result = authService.userRegistration(userSaveDto);

      assertTrue(result.equals(successMessage));
    }

    @Test
    @DisplayName("Test AuthServiceImp method Registration. Negative Scenario Email is On Deleted List.")
    void testRegistrationNegativeScenarioIsOnDeletedList() {
      when(deletedUsersService.emailExist(userSaveDto.getEmail())).thenReturn(true);

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.userRegistration(userSaveDto));

      assertEquals(HttpStatus.FORBIDDEN, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("The email (%s) has been deleted and is no longer accessible.", userSaveDto.getEmail()),
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(userSaveDto.getEmail());
    }

    @Test
    @DisplayName("Test AuthServiceImp method Registration. Negative Scenario Email is already in use.")
    void testRegistrationNegativeScenarioIsAlreadyInUse() {

      when(deletedUsersService.emailExist(userSaveDto.getEmail())).thenReturn(false);
      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.userRegistration(userSaveDto));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals("Email is already in use.",
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(userSaveDto.getEmail());
      verify(repository, times(1)).findUserByEmail(userSaveDto.getEmail());
    }

    @Test
    @DisplayName("Test AuthServiceImp method Registration. Negative Scenario Email already exists and needs confirmation.")
    void testRegistrationNegativeScenarioAlreadyExistsAndNeedsConfirmation() {
      String message = String.format("This email address (%s) already exists and needs confirmation.",
          userSaveDto.getEmail());

      userOne.setMailConfirmation(false);
      when(deletedUsersService.emailExist(userSaveDto.getEmail())).thenReturn(false);
      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.userRegistration(userSaveDto));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(userSaveDto.getEmail());
      verify(repository, times(1)).findUserByEmail(userSaveDto.getEmail());
    }
  }

  @Nested
  @DisplayName("Test AuthServiceImp method Login.")
  class Login {
    @Test
    @DisplayName("Test AuthController method Login Positive Scenario.")
    void testLogin() {

      String getAccessToken = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhbm9ueW1vdXNVc2VyIiwicm" +
          "9sZSI6IkFETUlOIiwiaWF0IjoxNzI4NzM2MzcwLCJleHAiOjE3Mjg3NzIzNzB9.sQs" +
          "6sV6GhkBXLkRs5JyiOW7SN0YdfFpyu7HrRp7x8PFFyq_biyHut4eTKynzMSbVwQCDRqFlL_b88RsverSmBA";

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
      when(jwtAuthentication.generateToken(anyString(), anyString(), any(Role.class))).thenReturn(getAccessToken);

      LoginResponse tokenPair = authService.login(loginDto);

      assertAll(
          () -> assertEquals(HttpStatus.OK.value(), tokenPair.getStatusCode()),
          () -> assertEquals(userOne.getId().toHexString(), tokenPair.getUserId()),
          () -> assertEquals(userOne.getName(), tokenPair.getUserName()),
          () -> assertFalse(tokenPair.getAccessToken().isEmpty())
      );

      verify(deletedUsersService, times(1)).emailExist(userSaveDto.getEmail());
      verify(repository, times(1)).findUserByEmail(userSaveDto.getEmail());
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Email is On Deleted List.")
    void testLoginNegativeScenarioIsOnDeletedList() {
      String message = String.format("The email (%s) has been deleted and is no longer accessible.", email);
      when(deletedUsersService.emailExist(email)).thenReturn(true);

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.login(loginDto));

      assertEquals(HttpStatus.FORBIDDEN, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(userSaveDto.getEmail());
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Email is not registered.")
    void testLoginNegativeScenarioIsNotRegistered() {
      String message = String.format("Email (%s) is not registered.", loginDto.getEmail());

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.login(loginDto));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(userSaveDto.getEmail());
      verify(repository, times(1)).findUserByEmail(userSaveDto.getEmail());
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Confirm your email.")
    void testLoginNegativeScenarioConfirmYourEmail() {
      String message = String.format("Confirm your email (%s)", loginDto.getEmail());

      userOne.setMailConfirmation(false);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.login(loginDto));

      assertEquals(HttpStatus.UNAUTHORIZED, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(email);
      verify(repository, times(1)).findUserByEmail(email);

    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario User banned.")
    void testLoginNegativeScenarioUserBanned() {
      String message = "User banned.";
      userOne.setStatus(Status.BANNED);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.login(loginDto));

      assertEquals(HttpStatus.FORBIDDEN, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(email);
      verify(repository, times(1)).findUserByEmail(email);
    }

    @Test
    @DisplayName("Test AuthController method login. Negative Scenario Wrong password.")
    void testLoginNegativeScenarioWrongPassword() {
      String message = "Wrong password";

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.findUserByEmail(email)).thenReturn(Optional.of(userOne));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.login(loginDto));

      assertEquals(HttpStatus.FORBIDDEN, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(email);
      verify(repository, times(1)).findUserByEmail(email);
    }
  }


  @Nested
  @DisplayName("Test AuthServiceImp method CheckExistEmailAndIsAccessible.")
  class CheckExistEmailAndIsAccessible {
    @Test
    @DisplayName("Test AuthController method CheckExistEmailAndIsAccessible Positive Scenario.")
    void checkExistEmailAndIsAccessibleScenarioIsNotUse() {
      String message = String.format("Email (%s) is not use.", email);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.existsByEmail(email)).thenReturn(false);

      EmailVerificationResponseDTO response = authService.checkExistEmailAndIsAccessible(email);

      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals(message, response.getMessage());
      assertFalse(response.getEmailExist());

      verify(deletedUsersService, times(1)).emailExist(email);
      verify(repository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Test AuthController method CheckExistEmailAndIsAccessible. Scenario Email already in use return true.")
    void checkExistEmailAndIsAccessibleScenarioAlreadyInUse() {
      String message = String.format("Email (%s) is already in use.", email);

      when(deletedUsersService.emailExist(email)).thenReturn(false);
      when(repository.existsByEmail(email)).thenReturn(true);

      EmailVerificationResponseDTO response = authService.checkExistEmailAndIsAccessible(email);

      assertEquals(HttpStatus.OK.value(), response.getStatusCode());
      assertEquals(message, response.getMessage());
      assertTrue(response.getEmailExist());

      verify(deletedUsersService, times(1)).emailExist(email);
      verify(repository, times(1)).existsByEmail(email);
    }

    @Test
    @DisplayName("Test AuthController method CheckExistEmailAndIsAccessible. Negative Scenario Email is On Deleted List.")
    void checkExistEmailAndIsAccessibleScenarioIsOnDeletedList() {
      String message = String.format("The email (%s) has been deleted and is no longer accessible.", email);

      when(deletedUsersService.emailExist(email)).thenReturn(true);

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.checkExistEmailAndIsAccessible(email));

      assertEquals(HttpStatus.FORBIDDEN, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(deletedUsersService, times(1)).emailExist(email);
    }
  }

  @Nested
  @DisplayName("Test AuthServiceImp method EmailVerificationCheck.")
  class EmailVerificationCheck {

    @Test
    @DisplayName("Test AuthServiceImp method emailVerificationCheck Positive Scenario.")
    void testMethodEmailVerificationCheckPositiveScenario() {

      String message = String.format("Email (%s) is confirmed", email);

      UserEmailData userEmailData = new UserEmailData(userOne.getId().toHexString(), userOne.getEmail(), password);
      User mockUser = new User();

      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));
      when(mailRepository.findByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userEmailData));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
      when(repository.save(any(User.class))).thenReturn(mockUser);
      doNothing().when(mailRepository).delete(any(UserEmailData.class));

      String responseMessage = authService.emailVerificationCheck(email, password);

      assertFalse(responseMessage.isEmpty());
      assertEquals(message, responseMessage);

      verify(repository, times(1)).findUserByEmail(userSaveDto.getEmail());
      verify(mailRepository, times(1)).findByEmail(userSaveDto.getEmail());
      verify(repository, times(1)).save(any(User.class));
      verify(mailRepository, times(1)).delete(any(UserEmailData.class));

    }

    @Test
    @DisplayName("Test AuthServiceImp method emailVerificationCheck. Negative Scenario User not found.")
    void testMethodEmailVerificationCheckNegativeScenarioUserNotFound() {
      String message = String.format("User not found email (%s).", email);

      when(repository.findUserByEmail(email)).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> authService.emailVerificationCheck(email, password));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(message,
          errorIfUserNotFound.getMessage());

      verify(repository, times(1)).findUserByEmail(email);

    }

    @Test
    @DisplayName("Test AuthServiceImp method emailVerificationCheck. Negative Scenario Wrong Password")
    void testMethodEmailVerificationCheckNegativeScenarioWrongPassword() {
      String message = "Wrong password";

      UserEmailData userEmailData = new UserEmailData(userOne.getId().toHexString(), userOne.getEmail(), "password");

      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));
      when(mailRepository.findByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userEmailData));
      when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

      String responseMessage = authService.emailVerificationCheck(email, password);

      assertFalse(responseMessage.isEmpty());
      assertEquals(message, responseMessage);

      verify(repository, times(1)).findUserByEmail(userSaveDto.getEmail());
      verify(mailRepository, times(1)).findByEmail(userSaveDto.getEmail());
      verify(passwordEncoder, times(1)).matches(anyString(), anyString());
    }

    @Test
    @DisplayName("Test AuthServiceImp method emailVerificationCheck. Negative Scenario Email not found (MailConfirmationRepository)")
    void testMethodEmailVerificationCheckNegativeScenarioEmailNotFound() throws Exception {

      String message = String.format("Email (%s) not found", email);

      when(repository.findUserByEmail(userSaveDto.getEmail())).thenReturn(Optional.of(userOne));
      when(mailRepository.findByEmail(userSaveDto.getEmail())).thenReturn(Optional.empty());

      String responseMessage = authService.emailVerificationCheck(email, password);

      assertFalse(responseMessage.isEmpty());
      assertEquals(message, responseMessage);

      verify(repository, times(1)).findUserByEmail(userSaveDto.getEmail());
      verify(mailRepository, times(1)).findByEmail(userSaveDto.getEmail());

    }
  }

  @Nested
  @DisplayName("Test AuthServiceImp method SendLetterToUser.")
  class SendLetterToUser {
    @Test
    @DisplayName("Test AuthServiceImp method SendLetterToUser. Scenario Message Is already In Gmail send folder.")
    void testSendLetterToUserScenarioMessageIsInGmail() {
      String messageExpect = "The user has such a letter in their correspondence.";

      when(mailService.getMessagesFromUser(email)).thenReturn(true);

      String message = authService.sendLetterToUser(email);

      assertFalse(message.isEmpty());
      assertEquals(messageExpect, message);

      verify(mailService, times(1)).getMessagesFromUser(email);
    }

    @Test
    @DisplayName("Test AuthServiceImp method SendLetterToUser. Scenario Message Is not In Gmail send folder.")
    void testSendLetterToUserScenarioMessageIsNotInGmail() {
      String messageExpect = "The message was sent to the user again.";

      when(mailService.getMessagesFromUser(email)).thenReturn(false);
      doNothing().when(mailService).sendHtmlEmailAfterRegistration(email);
      String message = authService.sendLetterToUser(email);

      assertFalse(message.isEmpty());
      assertEquals(messageExpect, message);

      verify(mailService, times(1)).getMessagesFromUser(email);
      verify(mailService, times(1)).sendHtmlEmailAfterRegistration(email);
    }
  }
}
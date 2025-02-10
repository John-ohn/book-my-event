package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.DeletedUsersService;
import com.BookMyEvent.service.MailService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class UserServiceImpTest {

  @Mock
  private UserRepository userRepository;

  @Mock
  private MailService mailService;

  @Mock
  private DeletedUsersService deletedUsersService;

  @Mock
  private UserMapper userMapper;

  @InjectMocks
  private UserServiceImp userService;

  private User userOne;

  private UserResponseDto userResponseDto;

  @BeforeEach
  void createUserOne() {

    userOne = User.builder()
        .id(new ObjectId("66c648b600179737a3d5c235"))
        .name("Ronald")
        .email("sewewt@code.com")
        .password("As123ertyuer")
        .location("Kyiv")
        .mailConfirmation(true)
        .status(Status.ACTIVE)
        .role(Role.VISITOR)
        .creationDate(LocalDateTime.now())
        .build();

    userResponseDto = UserResponseDto.builder()
        .id(userOne.getId().toHexString())
        .name(userOne.getName())
        .email(userOne.getEmail())
        .location(userOne.getLocation())
        .mailConfirmation(userOne.isMailConfirmation())
        .status(userOne.getStatus())
        .role(userOne.getRole())
        .creationDate(userOne.getCreationDate())
        .build();

  }

  @Test
  @DisplayName("Test UserService method FindAllUserProfiles")
  public void testFindAllUserProfiles() {
    when(userRepository.findAllUserProfiles()).thenReturn(List.of());

    List<UserResponseDto> resultEmptyList = userService.findAllUserProfiles();

    assertTrue(resultEmptyList.isEmpty());

    when(userRepository.findAllUserProfiles()).thenReturn(List.of(userResponseDto));

    List<UserResponseDto> result = userService.findAllUserProfiles();
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertTrue(result.contains(userResponseDto))
    );

    verify(userRepository, times(2)).findAllUserProfiles();
  }

  @Nested
  @DisplayName("Test UserService method FindUserInfoById.")
  class FindUserInfoById {
    @Test
    @DisplayName("Test UserService method FindUserInfoById. Positive Scenario User is Find by id.")
    public void testFindUserInfoByIdPositiveScenarioFindUser() {
      when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
      when(userMapper.toUserResponseDtoWithEvents(userOne)).thenReturn(userResponseDto);
      UserResponseDto responseDto = userService.findUserInfoById(userOne.getId().toHexString());
      assertAll(
          () -> assertEquals(userOne.getId().toHexString(), responseDto.getId()),
          () -> assertEquals(userOne.getEmail(), responseDto.getEmail()),
          () -> assertEquals(userOne.getRole(), responseDto.getRole())
      );

      verify(userRepository, times(1)).findById(userOne.getId());
    }

    @Test
    @DisplayName("Test UserService method FindUserInfoById. Negative Scenario User id is null or empty.")
    public void testFindUserInfoByIdNegativeScenarioUserIdIsNull() {

      GeneralException errorIfUserIdNull = assertThrows(GeneralException.class,
          () -> userService.findUserInfoById(null));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserIdNull.getHttpStatus());
      assertEquals("User ID cannot be null or empty",
          errorIfUserIdNull.getMessage());

      verify(userRepository, times(0)).findUserInfoById(userOne.getId().toHexString());
    }

    @Test
    @DisplayName("Test UserService method FindUserInfoById. Negative Scenario User Not Found.")
    public void testFindUserInfoByIdNegativeScenarioUserNotFound() {
      when(userRepository.findById(userOne.getId())).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.findUserInfoById(userOne.getId().toHexString()));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("User with ID [%s] not found.", userOne.getId().toHexString()),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findById(userOne.getId());
    }
  }

  @Nested
  @DisplayName("Test UserService method findUserInfoByEmail.")
  class FindUserInfoByEmail {
    @Test
    @DisplayName("Test UserService method findUserInfoByEmail. Positive Scenario User is Find by Email.")
    public void testFindUserInfoByEmailPositiveScenarioFindUser() {
      when(userRepository.findUserInfoByEmail(userOne.getEmail())).thenReturn(Optional.of(userResponseDto));

      UserResponseDto responseDto = userService.findUserInfoByEmail(userOne.getEmail());
      assertAll(
          () -> assertEquals(userOne.getId().toHexString(), responseDto.getId()),
          () -> assertEquals(userOne.getEmail(), responseDto.getEmail()),
          () -> assertEquals(userOne.getRole(), responseDto.getRole())
      );

      verify(userRepository, times(1)).findUserInfoByEmail(userOne.getEmail());
    }

    @Test
    @DisplayName("Test UserService method findUserInfoByEmail. Negative Scenario User Email is null or empty.")
    public void testFindUserInfoByEmailNegativeScenarioUserEmailIsNull() {

      GeneralException errorIfUserIdNull = assertThrows(GeneralException.class,
          () -> userService.findUserInfoByEmail(null));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserIdNull.getHttpStatus());
      assertEquals("User Email cannot be null or empty",
          errorIfUserIdNull.getMessage());

      verify(userRepository, times(0)).findUserInfoByEmail(userOne.getEmail());
    }

    @Test
    @DisplayName("Test UserService method findUserInfoByEmail. Negative Scenario User Not Found.")
    public void testFindUserInfoByEmailNegativeScenarioUserNotFound() {
      when(userRepository.findUserInfoByEmail(userOne.getEmail())).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.findUserInfoByEmail(userOne.getEmail()));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("User with Email [%s] not found.", userOne.getEmail()),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserInfoByEmail(userOne.getEmail());
    }
  }

  @Test
  void save() {
  }

  @Nested
  @DisplayName("Test UserService method updateFieldsFromAdmin")
  class UpdateFieldsFromAdmin {
    @Test
    @DisplayName("Test UserService method updateFieldsFromAdmin. Positive Scenario User is Updated.")
    void testUpdateFieldsFromAdminPositiveScenarioUpdatedUser() {

    }
  }

  @Test
  void delete() {
  }

  @Nested
  @DisplayName("Test UserService method DeleteFromAdmin")
  class DeleteFromAdmin {
    @Test
    @DisplayName("Test UserService method deleteFromAdmin. Positive Scenario User was Deleted.")
    void testDeleteFromAdminPositiveScenarioUserDeleted() {
      when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
      doNothing().when(deletedUsersService).addUserToDeletedList(userOne.getEmail());
      doNothing().when(userRepository).delete(userOne);

      String responseMessage = userService.deleteFromAdmin(userOne.getId().toHexString());
      assertAll(
          () -> assertFalse(responseMessage.isEmpty()),
          () -> assertEquals("User was deleted successfully.", responseMessage)
      );

      verify(userRepository, times(1)).findById(userOne.getId());
    }

    @Test
    @DisplayName("Test UserService method deleteFromAdmin. Negative Scenario User id is null or empty.")
    public void testDeleteFromAdminNegativeScenarioUserIdIsNull() {

      GeneralException errorIfUserIdNull = assertThrows(GeneralException.class,
          () -> userService.deleteFromAdmin(null));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserIdNull.getHttpStatus());
      assertEquals(String.format("User ID cannot be null or empty. %s ", null),
          errorIfUserIdNull.getMessage());

      verify(userRepository, times(0)).findUserInfoByEmail(userOne.getEmail());
    }

    @Test
    @DisplayName("Test UserService method deleteFromAdmin. Negative Scenario User Not Found.")
    public void testDeleteFromAdminNegativeScenarioUserNotFound() {
      when(userRepository.findById(userOne.getId())).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.deleteFromAdmin(userOne.getId().toHexString()));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("User with ID [%s] not found.", userOne.getId().toHexString()),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findById(userOne.getId());
    }
  }

  @Nested
  @DisplayName("Test UserService method Banned")
  class UserBannedTests {
    @Test
    @DisplayName("Test UserService method Banned Positive Scenario")
    void testMethodBannedPositiveScenario() {
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));
      when(userRepository.save(userOne)).thenReturn(userOne);
      doNothing().when(mailService).sendSimpleHtmlMailMessage4Line(anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString());
      String user = userService.banned(userOne.getEmail());

      assertEquals("User status updated to 'BANNED'", user);

      verify(userRepository, times(1)).findUserByEmail(userOne.getEmail());
      verify(userRepository, times(1)).save(userOne);
    }

    @Test
    @DisplayName("Test UserService method Banned Negative Scenario. User with email not found.")
    void testMethodBannedNegativeScenarioNotFound() {
      String notExistEmail = "sewewtnot@code.com";
      when(userRepository.findUserByEmail(notExistEmail)).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.banned(notExistEmail));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("User with such email: (%s) not found", notExistEmail),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(notExistEmail);
      verify(userRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("Test UserService method Banned Negative Scenario. User is already banned")
    void testMethodBannedNegativeScenarioAlreadyBanned() {
      userOne.setStatus(Status.BANNED);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.banned(userOne.getEmail()));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals("User is already banned", errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(userOne.getEmail());
      verify(userRepository, times(0)).save(any());
    }


  }

  @Nested
  @DisplayName("Test UserService method Unban.")
  class UserUnbanTests {

    @Test
    @DisplayName("Test UserService method Unban Positive Scenario.")
    void testMethodUnbanPositiveScenario() {
      userOne.setStatus(Status.BANNED);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));
      when(userRepository.save(userOne)).thenReturn(userOne);

      doNothing().when(mailService).sendSimpleHtmlMailMessage4Line(anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString());

      String user = userService.unbanned(userOne.getEmail());

      assertEquals("User activated successfully", user);
    }

    @Test
    @DisplayName("Test UserService method Unban Negative Scenario. User with email not found.")
    void testMethodUnbanNegativeScenarioNotFound() {
      String notExistEmail = "sewewtnot@code.com";
      when(userRepository.findUserByEmail(notExistEmail)).thenReturn(Optional.empty());

      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.banned(notExistEmail));

      assertEquals(HttpStatus.NOT_FOUND, errorIfUserNotFound.getHttpStatus());
      assertEquals(String.format("User with such email: (%s) not found", notExistEmail),
          errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(notExistEmail);
      verify(userRepository, times(0)).save(any());
    }


    @Test
    @DisplayName("Test UserService method Unban Negative Scenario. User is already active.")
    void testMethodUnbanNegativeScenarioAlreadyActive() {
      userOne.setStatus(Status.ACTIVE);
      when(userRepository.findUserByEmail(userOne.getEmail())).thenReturn(Optional.of(userOne));

//      String alreadyActive = userService.unbanned(userOne.getEmail());
//      assertEquals("User is already active", alreadyActive);
      GeneralException errorIfUserNotFound = assertThrows(GeneralException.class,
          () -> userService.unbanned(userOne.getEmail()));

      assertEquals(HttpStatus.BAD_REQUEST, errorIfUserNotFound.getHttpStatus());
      assertEquals("User is already active", errorIfUserNotFound.getMessage());

      verify(userRepository, times(1)).findUserByEmail(userOne.getEmail());
      verify(userRepository, times(0)).save(any());
    }
  }
}
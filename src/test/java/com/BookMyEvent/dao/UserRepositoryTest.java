package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataMongoTest
@TestPropertySource(
    locations = "classpath:integrationtest.properties")
class UserRepositoryTest {

  @Autowired
  private UserRepository userRepository;

  private User userOne;

  private User userSecond;

  @BeforeEach
  void createUserOne() {
    userRepository.deleteAll();

    userOne = User.builder()
        .id(new ObjectId("66c648b600179737a3d5c235"))
        .name("Ronald")
        .email("sewewt@code.com")
        .password("As123ertyuer")
        .location("Kyiv")
        .mailConfirmation(true)
        .status(Status.ACTIVE)
        .role(Role.VISITOR)
        .phoneNumber("+380992343215")
        .creationDate(LocalDateTime.now())
        .build();

    userSecond = User.builder()
        .id(new ObjectId("66c648b600179737a3d5ce72"))
        .name("Ronald")
        .email("sewewcct@code.com")
        .password("As123ertyuer")
        .location("Kyiv")
        .mailConfirmation(true)
        .status(Status.ACTIVE)
        .role(Role.VISITOR)
        .phoneNumber("+380992343342")
        .creationDate(LocalDateTime.now())
        .build();
  }

  @Test
  @DisplayName("Test UserRepository method Save")
  void testMethodSavePositiveScenario(){
    User savedUser = userRepository.save(userOne);

    assertAll(
        () -> assertNotNull(savedUser),
        () -> assertEquals(userOne.getId(), savedUser.getId()),
        () -> assertEquals(userOne.getEmail(), savedUser.getEmail()),
        () -> assertEquals(userOne.getName(), savedUser.getName())
    );
  }

  @Test
  @DisplayName("Test UserRepository method FindUserInfoByEmail")
  void testMethodFindUserInfoByEmailPositiveScenario() {

    userRepository.save(userOne);

    Optional<UserResponseDto> user = userRepository.findUserInfoByEmail(userOne.getEmail());

    assertAll(
        () -> assertNotNull(user),
        () -> assertEquals(userOne.getId().toHexString(), user.get().getId()),
        () -> assertEquals(userOne.getEmail(), user.get().getEmail()),
        () -> assertEquals(userOne.getName(), user.get().getName())

    );
  }

  @Test
  @DisplayName("Test UserRepository method FindUserInfoById")
  void testMethodFindUserInfoByIdPositiveScenario() {

    userRepository.save(userOne);

    Optional<UserResponseDto> user = userRepository.findUserInfoById(userOne.getId().toHexString());

    assertAll(
        () -> assertTrue(user.isPresent()),
        () -> assertEquals(userOne.getId().toHexString(), user.get().getId()),
        () -> assertEquals(userOne.getEmail(), user.get().getEmail()),
        () -> assertEquals(userOne.getName(), user.get().getName())
    );

  }

  @Test
  @DisplayName("Test UserRepository method FindAllUserProfiles")
  void testMethodFindAllUserProfilesPositiveScenario() {
    userRepository.save(userOne);
    userRepository.save(userSecond);
    List<UserResponseDto> listUsers = userRepository.findAllUserProfiles();

    assertAll(
        () -> assertNotNull(listUsers),
        () -> assertEquals(2, listUsers.size()),
        () -> assertEquals(userOne.getId().toHexString(), listUsers.get(0).getId())
    );
  }

  @Test
  @DisplayName("Test UserRepository method FindUserByEmail")
  void testMethodFindUserByEmailPositiveScenario() {
    userRepository.save(userOne);
    Optional<User> user = userRepository.findUserByEmail(userOne.getEmail());

    assertAll(
        () -> assertNotNull(user),
        () -> assertEquals(userOne.getId(), user.get().getId()),
        () -> assertEquals(userOne.getEmail(), user.get().getEmail()),
        () -> assertEquals(userOne.getName(), user.get().getName())
    );
  }

  @Test
  @DisplayName("Test UserRepository method ExistsByEmail")
  void testMethodExistsByEmailPositiveScenario() {
    userRepository.save(userOne);
    boolean  existsByEmail = userRepository.existsByEmail(userOne.getEmail());
    boolean  notExistsByEmail = userRepository.existsByEmail("sewewcctawe@code.com");

    assertTrue(existsByEmail);
    assertFalse(notExistsByEmail);
  }

  @Test
  @DisplayName("Test UserRepository method Delete")
  void testMethodDeletePositiveScenario(){
    userRepository.save(userOne);
    userRepository.deleteById(userOne.getId());
    Optional<User> user = userRepository.findUserByEmail(userOne.getEmail());

        assertFalse(user.isPresent());
  }

}
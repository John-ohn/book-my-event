package com.BookMyEvent.controller;

import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.DeletedUsersService;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(
    locations = "classpath:integrationtest.properties")
@Slf4j
class AdminControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Mock
  private UserService userService;
  @MockBean
  private EventService eventService;

  @MockBean
  private UserRepository userRepository;
  @MockBean
  private DeletedUsersService deletedUsersService;
  @MockBean
  private UserMapper userMapper;
  @MockBean
  private MailService mailService;

  private User userOne;

  private UserResponseDto userResponseDto;

  @BeforeEach
  void createUserOne() {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  void findAllUserProfiles() throws Exception {

    when(userRepository.findAllUserProfiles()).thenReturn(List.of(userResponseDto));

    mockMvc.perform(get("/admin/users"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$.length()").value(1))
        .andExpect(jsonPath("$[*].id", containsInAnyOrder(userResponseDto.getId())))
        .andExpect(jsonPath("$[0].email").value(userResponseDto.getEmail()));
  }

  @Nested
  @DisplayName("Test AdminController method deleteUser.")
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  class DeleteUser {
    @Test
    @DisplayName("Test AdminController method deleteUser. Positive Scenario")
    void testMethodDeleteUserPositiveScenario() throws Exception {
      String successMessage = "User was deleted successfully.";
      when(userRepository.findById(userOne.getId())).thenReturn(Optional.of(userOne));
      doNothing().when(deletedUsersService).addUserToDeletedList(userOne.getEmail());
      doNothing().when(userRepository).delete(userOne);

      mockMvc.perform(delete("/admin/users/{userId}", userOne.getId().toHexString()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(successMessage)));
    }

    @Test
    @DisplayName("Test AdminController method deleteUser. Negative Scenario User not found")
    void testMethodDeleteUserNegativeScenarioUserNotFound() throws Exception {
      String errorMessage = String.format("User with ID [%s] not found.", userResponseDto.getId());

      when(userRepository.findById(userOne.getId())).thenReturn(Optional.empty());

      mockMvc.perform(delete("/admin/users/{userId}", userOne.getId().toHexString()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
          .andExpect(jsonPath("$.message", is(errorMessage)));
    }
  }

  @Nested
  @DisplayName("Test AdminController method BanUser.")
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  class BanUser {
    @Test
    @DisplayName("Test AdminController method deleteUser. Positive Scenario User is BANNED")
    void testMethodBanUserPositiveScenario() throws Exception {
      String successMessage = "User status updated to 'BANNED'";

      when(userRepository.findUserByEmail(userResponseDto.getEmail())).thenReturn(Optional.of(userOne));
      when(userRepository.save(userOne)).thenReturn(userOne);
      doNothing().when(mailService).sendSimpleHtmlMailMessage4Line(
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString());

      mockMvc.perform(patch("/admin/users/ban/{email}", userResponseDto.getEmail()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(successMessage)));
    }

    @Test
    @DisplayName("Test AdminController method deleteUser. Negative Scenario User not found")
    void testMethodBanUserNegativeScenarioUserNotFound() throws Exception {
      String errorMessage = String.format("User with such email: (%s) not found", userResponseDto.getEmail());

      when(userRepository.findUserByEmail(userResponseDto.getEmail())).thenReturn(Optional.empty());

      mockMvc.perform(patch("/admin/users/ban/{email}", userResponseDto.getEmail()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
          .andExpect(jsonPath("$.message", is(errorMessage)));
    }

    @Test
    @DisplayName("Test AdminController method deleteUser. Negative Scenario User is already banned")
    void testMethodBanUserNegativeScenarioUserAlreadyBanned() throws Exception {
      String errorMessage = "User is already banned";

      userOne.setStatus(Status.BANNED);
      when(userRepository.findUserByEmail(userResponseDto.getEmail())).thenReturn(Optional.of(userOne));

      mockMvc.perform(patch("/admin/users/ban/{email}", userResponseDto.getEmail()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
          .andExpect(jsonPath("$.message", is(errorMessage)));
    }
  }


  @Nested
  @DisplayName("Test AdminController method unbanUser.")
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  class UnbanUser {

    @Test
    @DisplayName("Test AdminController method unbanUser. Positive Scenario User is Unban")
    void testMethodUnbanUserPositiveScenario() throws Exception {
      String successMessage = "User activated successfully";
      userOne.setStatus(Status.BANNED);
      when(userRepository.findUserByEmail(userResponseDto.getEmail())).thenReturn(Optional.of(userOne));
      when(userRepository.save(userOne)).thenReturn(userOne);
      doNothing().when(mailService).sendSimpleHtmlMailMessage4Line(
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString(),
          anyString());

      mockMvc.perform(patch("/admin/users/unban/{email}", userResponseDto.getEmail()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.message", is(successMessage)));
    }

    @Test
    @DisplayName("Test AdminController method unbanUser. Negative Scenario User not found")
    void testMethodUnbanUserNegativeScenarioUserNotFound() throws Exception {
      String errorMessage = String.format("User with such email: (%s) not found", userResponseDto.getEmail());

      when(userRepository.findUserByEmail(userResponseDto.getEmail())).thenReturn(Optional.empty());

      mockMvc.perform(patch("/admin/users/unban/{email}", userResponseDto.getEmail()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.status", is(HttpStatus.NOT_FOUND.value())))
          .andExpect(jsonPath("$.message", is(errorMessage)));
    }

    @Test
    @DisplayName("Test AdminController method unbanUser. Negative Scenario User is already unban")
    void testMethodUnbanUserNegativeScenarioUserAlreadyBanned() throws Exception {
      String errorMessage = "User is already active";

      userOne.setStatus(Status.ACTIVE);
      when(userRepository.findUserByEmail(userResponseDto.getEmail())).thenReturn(Optional.of(userOne));

      mockMvc.perform(patch("/admin/users/unban/{email}", userResponseDto.getEmail()))
          .andExpect(status().isOk())
          .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
//          .andExpect(jsonPath("$.status", is(HttpStatus.BAD_REQUEST.value())))
          .andExpect(jsonPath("$.message", is(errorMessage)));
    }
  }

  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  void getCountByStatus() throws Exception {
    Map<String, Integer> countByStatus = new HashMap<>();
    countByStatus.put(EventStatus.CANCELLED.toString(),0);
    countByStatus.put(EventStatus.PENDING.toString(),1);
    countByStatus.put(EventStatus.APPROVED.toString(),2);

    when(eventService.countByStatus()).thenReturn(countByStatus);

    mockMvc.perform(get("/admin/events/count/status"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.CANCELLED").value(0))
        .andExpect(jsonPath("$.APPROVED").value(2))
        .andExpect(jsonPath("$.PENDING").value(1));
  }

  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  void getEventsByStatusPENDING() throws Exception {
    LocalTime localTime =  LocalTime.now();
    EventResponseDto event = new EventResponseDto();
    event.setId("66c648b600179737a3d5c235");
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    event.setEventCategory(EventCategory.TOP_EVENTS.toString());
    event.setEventStatus(EventStatus.PENDING.toString());
    event.setAvailableTickets(100);
    event.setNumberOfTickets(0);
    event.setDate(new DateDetails(LocalDate.of(2025, 10, 21).toString(),
        localTime.toString(),
        localTime.plusHours(2L).toString()));
    List<EventResponseDto> results = List.of(event);

    when(eventService.getEventsByStatus(EventStatus.PENDING.toString())).thenReturn(results);

    String responseBody = objectMapper.writeValueAsString(results);
    log.info("results " +results);
    log.info("results " +responseBody);

    mockMvc.perform(get("/admin/events/status/{status}",EventStatus.PENDING.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[*].id", containsInAnyOrder(event.getId())))
        .andExpect(jsonPath("$[0].eventStatus").value(event.getEventStatus().toString()));
  }

  @Test
  @WithMockUser(username = "admin", roles = {"ADMIN"})
  void getEventsByStatusAPPROVED() throws Exception {
    LocalTime  localTime =  LocalTime.now();
    EventResponseDto event = new EventResponseDto();
    event.setId("66c648b600179737a3d5c235");
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    event.setEventCategory(EventCategory.TOP_EVENTS.toString());
    event.setEventStatus(EventStatus.APPROVED.toString());
    event.setAvailableTickets(100);
    event.setNumberOfTickets(0);
    event.setDate(new DateDetails(LocalDate.of(2025, 10, 21).toString(),
        localTime.toString(),
        localTime.plusHours(2L).toString()));
    List<EventResponseDto> results = List.of(event);

    when(eventService.getEventsByStatus(EventStatus.APPROVED.toString())).thenReturn(results);

    String responseBody = objectMapper.writeValueAsString(results);
    log.info("results " +results);
    log.info("results " +responseBody);

    mockMvc.perform(get("/admin/events/status/{status}",EventStatus.APPROVED.toString()))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[*].id", containsInAnyOrder(event.getId())))
        .andExpect(jsonPath("$[0].eventStatus").value(event.getEventStatus().toString()));
  }

}
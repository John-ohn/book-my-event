package com.BookMyEvent.controller;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@Tag(name = "Admin Endpoints")
@RequestMapping("/admin")
@Slf4j
@RequiredArgsConstructor
public class AdminController {

  private final UserService userService;
  private final EventService eventService;

  private String className = this.getClass().getSimpleName();
  @Operation(
      summary = "Get  list of users",
      description = "Retrieves a list of users.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = " List of users successfully retrieved",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      array = @ArraySchema(schema = @Schema( implementation = UserResponseDto.class))
                  )}
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid page or size parameter",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class)
                  )
              })
      })
  @GetMapping("/users")
  public ResponseEntity<List<UserResponseDto>> findAllUserProfiles() {
    List<UserResponseDto> userList = userService.findAllUserProfiles();
    log.info("{}::findAllUsers - /admin/users - Return list of user.", this.getClass().getSimpleName());
    return ResponseEntity.ok(userList);
  }

  @Operation(
      summary = "Get USER info by id.",
      description = "Get USER info by id.",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = " Return USER info.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = UserResponseDto.class)
                  )}
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Bad request or not Validation failed. User ID cannot be null or empty",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class))
              }),
          @ApiResponse(
              responseCode = "404",
              description = "User with ID not found.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class)
                  )
              }),
          @ApiResponse(
              responseCode = "403",
              description = "Access forbidden. Can use Admin.",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(
                      example = "{\n" +
                          "    \"timestamp\": \"2024-12-19T16:40:54.575+00:00\",\n" +
                          "    \"status\": 403,\n" +
                          "    \"error\": \"Forbidden\",\n" +
                          "    \"path\": \"/api/v1/users/674cb373e84f0654529647c4\"\n" +
                          "}"
                  )
              )
          )
      })
  @GetMapping("/users/{userId}")
  public ResponseEntity<UserResponseDto> findUserInfoById(@PathVariable("userId") String userId) {
    UserResponseDto userResponse = userService.findUserInfoById(userId);
    log.info("UserController::findUserInfoById - /users/{userId} - Return User Info email: {} .", userResponse.getEmail());
    return ResponseEntity.ok(userResponse);
  }

  @Operation(
      summary = "Get All Events",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Get All Events to admin. Sorted by TOP_EVENTS and CreationDate.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = Event.class))
              })
      })
  @GetMapping("/events")
  public ResponseEntity<List<EventResponseDto>> getAllEvents() {
    log.info("Class: {}, Method: getAllEvents - Fetching all events.", this.getClass().getSimpleName());
    List<EventResponseDto> events = eventService.getAllEvents();
    return ResponseEntity.ok(events);
  }

  @Operation(
      summary = "Get Event by eventId",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Get info by Event.",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = EventResponseDto.class))
              }),
          @ApiResponse(
              responseCode = "404",
              description = "Event with the provided id not found",
              content = {
                  @Content(
                      mediaType = APPLICATION_JSON_VALUE,
                      schema = @Schema(implementation = ErrorResponseDto.class)
                  )
              })
      })
  @GetMapping("/events/{eventId}")
  public ResponseEntity<EventResponseDto> getEventById(@PathVariable("eventId") String eventId) {
    log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
    EventResponseDto events = eventService.getEventById(eventId);
    return ResponseEntity.ok(events);
  }


  @PatchMapping("/{id}/approve")
  public ResponseEntity<EventResponseDto> approveEvent(@PathVariable String id) {
    log.info("Class: {}, Method: approveEvent - Approving event with ID: {}", this.getClass().getSimpleName(), id);
    EventResponseDto approvedEvent = eventService.approveEvent(id);
    return ResponseEntity.ok(approvedEvent);
  }

  @Operation(
      summary = "Delete a user by ID",
      description = "Deletes a user from the system using their unique user ID. If the user ID is not provided, invalid, or not found, an error is returned.",
      parameters = {
          @Parameter(
              name = "userId",
              description = "The unique identifier of the user to be deleted",
              required = true,
              example = "1229316345"
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "User deleted successfully",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = AppResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Invalid user ID or missing ID",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "User not found",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          )
      }
  )
  @DeleteMapping("/users/{userId}")
  public ResponseEntity<AppResponse> deleteUser(@PathVariable("userId") String userId) {
    AppResponse response = new AppResponse(
        HttpStatus.OK.value(), userService.deleteFromAdmin(userId));
    log.info("{}::delete - /users/{userId} - Return deletion message.", this.getClass().getSimpleName());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Ban a user",
      description = "Sets the status of a specified user to 'BANNED', restricting their access to the system. If the user is already banned, an error is returned.",
      parameters = {
          @Parameter(
              name = "email",
              description = "Email of the user to ban",
              required = true,
              example = "user@example.com"
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "User status updated to 'BANNED'",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = AppResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "User already banned or not found. User with the specified email not found",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          ),
      }
  )
  @PatchMapping("/users/ban/{email}")
  public ResponseEntity<AppResponse> banUser(@PathVariable("email") String email) {
    AppResponse response = new AppResponse(HttpStatus.OK.value(), userService.banned(email));
    log.info("{}::banUser - /users/ban/{email} - Return ban message.", this.getClass().getSimpleName());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @Operation(
      summary = "Unban a user",
      description = "Sets the status of a specified user to 'ACTIVE', allowing them access to the system again. If the user is already active, no change is made.",
      parameters = {
          @Parameter(
              name = "email",
              description = "Email of the user to unban",
              required = true,
              example = "example@example.com"
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "User successfully unbanned or already active",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = AppResponse.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "User not found with the specified email",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          )
      }
  )
  @PatchMapping("/users/unban/{email}")
  public ResponseEntity<AppResponse> unbanUser(@PathVariable("email") String email) {
    AppResponse response = new AppResponse(HttpStatus.OK.value(), userService.unbanned(email));
    log.info("{}::unbanUser - /users/unban/{email} - Returned unban user message.", this.getClass().getSimpleName());
    return ResponseEntity.status(HttpStatus.OK).body(response);
  }

  @GetMapping("/events/count/status")
  public ResponseEntity<Map<String, Integer>> getCountByStatus() {
    log.info("Class: {}, Method: getCountByStatus - Counting events for all statuses", this.getClass().getSimpleName());

    Map<String, Integer> statusCountMap = eventService.countByStatus();

    log.info("Events count by status: {}", statusCountMap);
    return ResponseEntity.ok(statusCountMap);
  }

  @GetMapping("/events/status/{status}")
  public ResponseEntity<List<EventResponseDto>> getEventsByStatus(@PathVariable("status") String status) {
    log.info("Class: {}, Method: getEventsByStatus - Fetching events with status: {}", this.getClass().getSimpleName(), status);
    List<EventResponseDto> events = eventService.getEventsByStatus(status.toUpperCase());

    return ResponseEntity.ok(events);
  }

  @Operation(
      summary = "Update Event Status",
      description = "Updates the status of the event to one of the predefined values: PENDING, APPROVED, or CANCELLED.",
      parameters = {
          @Parameter(
              name = "eventsId",
              description = "The unique identifier of the event to update",
              required = true,
              example = "event123"
          ),
          @Parameter(
              name = "status",
              description = "The new status of the event",
              required = true,
              example = "APPROVED"
          )
      },
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Event status updated successfully",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = EventDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Event not found",
              content = @Content(
                  mediaType = APPLICATION_JSON_VALUE,
                  schema = @Schema(implementation = ErrorResponseDto.class)
              )
          )
      }
  )
  @PatchMapping("/events/{eventsId}/status")
  public ResponseEntity<EventResponseDto> updateEventStatus(@PathVariable String eventsId, @RequestParam String status) {
    log.info("Class: {}, Method: updateEventStatus - Updating status of event ID: {} to {}", this.getClass().getSimpleName(), eventsId, status);
    EventResponseDto updatedEvent = eventService.updateEventStatus(eventsId, status);
    return ResponseEntity.ok(updatedEvent);
  }

  @Operation(
      summary = "Delete not linked images",
      description = "Delete not linked images"
  )
  @DeleteMapping("/img/img")
  public ResponseEntity<String> deleteNotLinkedImg() {
    log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
    eventService.deleteNotLinkedImg();
    return ResponseEntity.ok("deleteNotLinkedImg - ok");
  }

}

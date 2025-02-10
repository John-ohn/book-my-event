package com.BookMyEvent.controller;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.dto.AppResponse;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.exception.model.ErrorResponseDto;
import com.BookMyEvent.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.BookMyEvent.config.SwaggerConfig.CREATED_EVENT_PAYLOAD_SCHEMA;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@Tag(name = "Events Controller")
@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private String className = this.getClass().getSimpleName();

    @Operation(
        summary = "Create a new event",
        description = CREATED_EVENT_PAYLOAD_SCHEMA,
        responses = {
            @ApiResponse(
                responseCode = "201",
                description = "Event created successfully",
                content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = EventDTO.class))
            ),
            @ApiResponse(responseCode = "400",
                description = "Invalid request (e.g., validation errors, file size too large)",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class)
                    )
                })
        }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponseDto> createEvent(
        @Parameter(description = "Event details, including the name, description, and date", required = true)
        @RequestPart("event") @Valid EventDTO eventDTO,
        @Parameter(description = "First event image (Max 1MB)", required = true)
        @RequestPart(value = "firstImage", required = true) MultipartFile firstImage,
        @Parameter(description = "Second event image (Max 1MB)", required = false)
        @RequestPart(value = "secondImage", required = false) MultipartFile secondImage,
        @Parameter(description = "Third event image (Max 1MB)", required = false)
        @RequestPart(value = "thirdImage", required = false) MultipartFile thirdImage) {
        log.info("Class: {}, Method: createEvent - firstImageBase64 {}", className, firstImage.getOriginalFilename());
        EventResponseDto createdEvent = eventService.createEvent(eventDTO, firstImage, secondImage, thirdImage);
        log.info("Class: {}, Method: createEvent - Creating new event", className);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdEvent);
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventResponseDto> updateEvent(@PathVariable String id, @RequestBody EventDTO eventDTO) {
        log.info("Class: {}, Method: updateEvent - Updating event with id: {}", className, id);
        EventResponseDto updatedEvent = eventService.updateEvent(id, eventDTO);
        return ResponseEntity.ok(updatedEvent);
    }

    @PatchMapping(path = "/{id}/image",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EventResponseDto> updateEventImage(@PathVariable String id,
    @Parameter(description = "User Avatar  image (Max 1MB)", required = true)
    @RequestPart(value = "eventImage", required = true) MultipartFile eventImage) {
        log.info("Class: {}, Method: updateEventImage - Updating Img event with id: {}", className, id);
        EventResponseDto updatedEvent = eventService.updateEventImage(id, eventImage);
        return ResponseEntity.ok(updatedEvent);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<AppResponse> deleteEvent(@PathVariable String id) {
        eventService.deleteEvent(id);
        AppResponse response = new AppResponse(
            HttpStatus.OK.value(), "Event deleted successfully");
        log.info("Class: {}, Method: deleteEvent - Deleting event with id: {}", className, id);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @Operation(
        summary = "Get All APPROVED Events",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Get All APPROVED Events.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = Event.class))
                })
        })
    @GetMapping
    public ResponseEntity<List<EventResponseDto>> getAllApprovedEvents() {
        log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
        List<EventResponseDto> events = eventService.getApprovedEvents();
        return ResponseEntity.ok(events);
    }

    @Operation(
        summary = "Get Approved Event by eventId",
        responses = {
            @ApiResponse(
                responseCode = "200",
                description = "Get info by Approved Event.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = EventResponseDto.class))
                }),
            @ApiResponse(
                responseCode = "404",
                description = "Event with the provided id not found or id is from not approved event.",
                content = {
                    @Content(
                        mediaType = APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ErrorResponseDto.class)
                    )
                })
        })
    @GetMapping("/{eventId}")
    public ResponseEntity<EventResponseDto> getEventById(@PathVariable("eventId") String eventId) {
        log.info("Class: {}, Method: getAllEventsUA - Fetching all APPROVED events.", className);
        EventResponseDto events = eventService.getApprovedEventById(eventId);
        return ResponseEntity.ok(events);
    }

    @DeleteMapping("/clearPastEvents")
    public ResponseEntity<AppResponse> clearPastEvents() {
        log.info("Class: {}, Method: clearPastEvents - Clearing past events.", className);
        eventService.deletePastEvents();
        AppResponse response = new AppResponse(
            HttpStatus.OK.value(), "All Event deleted successfully");
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
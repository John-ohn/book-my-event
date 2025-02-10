package com.BookMyEvent.controller;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.service.EventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Slf4j
@TestPropertySource(
    locations = "classpath:integrationtest.properties")
class EventControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @MockBean
  private EventService eventService;

  @Test
  void getAllEventsUA() throws Exception {
    LocalTime  localTime =  LocalTime.now();
    EventResponseDto event = new EventResponseDto();
    event.setId("66c648b600179737a3d5c235");
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS.getUkrainianName());
    event.setEventCategory(EventCategory.TOP_EVENTS.toString());
    event.setAvailableTickets(100);
    event.setNumberOfTickets(0);
    event.setDate(new DateDetails(LocalDate.of(2025, 10, 21).toString(),
        localTime.toString(),
        localTime.plusHours(2L).toString()));
    List<EventResponseDto> results = List.of(event);

    when(eventService.getApprovedEvents()).thenReturn(results);

    String responseBody = objectMapper.writeValueAsString(results);
    log.info("results " +results);
    log.info("results " +responseBody);

    mockMvc.perform(get("/events"))
        .andExpect(status().isOk())
        .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
        .andExpect(jsonPath("$", hasSize(1)))
        .andExpect(jsonPath("$[*].id", containsInAnyOrder(event.getId())))
        .andExpect(jsonPath("$[0].type").value(event.getEventType()));
  }

  @Test
  void updateEvent() throws Exception {
    String eventId = "66c648b600179737a3d5c235";
    EventDTO eventDTO = new EventDTO();
    eventDTO.setTitle("Updated Event");
    eventDTO.setDescription("Updated Description");

    eventDTO.setNumberOfTickets(200);
    eventDTO.setAvailableTickets(150);
    eventDTO.setLocation(new Location("City", "Street", "Venue", "23,3366", "435,2787"));

    EventResponseDto updatedEventDTO = new EventResponseDto();
    updatedEventDTO.setId(eventId);
    updatedEventDTO.setTitle("Updated Event");

    when(eventService.updateEvent(eq(eventId), eq(eventDTO))).thenReturn(updatedEventDTO);

    mockMvc.perform(put("/events/{id}", eventId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(eventDTO)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(eventId))
            .andExpect(jsonPath("$.title").value("Updated Event"));
  }

  @Test
  void deleteEvent() throws Exception {
    String eventId = "66c648b600179737a3d5c235";
    doNothing().when(eventService).deleteEvent(eventId);

    mockMvc.perform(delete("/events/{id}", eventId))
            .andExpect(status().isNoContent());
  }
}
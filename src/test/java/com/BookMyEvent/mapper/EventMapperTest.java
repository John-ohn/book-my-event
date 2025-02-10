package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class EventMapperTest {
  private EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

  @Test
  void toEventResponseDtoFromEvent() {
    Event event = new Event();
    event.setId(new ObjectId("671ffa8e7efcb743c34ed1ff"));
    event.setTitle("Event Title");
    event.setDescription("Event Description");
    event.setPhotoUrl("http://photo.url");
    event.setPhoneNumber("123456789");
    event.setTicketPrice(1000L);
    event.setNumberOfTickets(100);
    event.setAvailableTickets(50);
    event.setLocation(new Location("city", "street", "venue", "24,344", "64,323"));
    event.setEventType(EventType.SPORTS_EVENTS);
    event.setEventCategory(EventCategory.TOP_EVENTS);
    event.setImages(List.of());
    LocalTime  localTime =  LocalTime.now();
    EventResponseDto eventResponseDto = eventMapper.toEventResponseDtoFromEvent(event, new UserResponseDto());
    log.info("eventResponseDto : {}", eventResponseDto);

    assertEquals(event.getId().toHexString(), eventResponseDto.getId());
    assertEquals(event.getTitle(), eventResponseDto.getTitle());
    assertEquals(event.getDescription(), eventResponseDto.getDescription());
    assertEquals(event.getPhotoUrl(), eventResponseDto.getPhotoUrl());
    assertEquals(event.getPhoneNumber(), eventResponseDto.getPhoneNumber());
    assertEquals(event.getTicketPrice(), eventResponseDto.getTicketPrice());
    assertEquals(event.getNumberOfTickets(), eventResponseDto.getNumberOfTickets());
    assertEquals(event.getAvailableTickets(), eventResponseDto.getAvailableTickets());
    assertEquals(event.getLocation(), eventResponseDto.getLocation());
    assertEquals(EventType.SPORTS_EVENTS.getUkrainianName(), eventResponseDto.getEventType());
    assertEquals(EventCategory.TOP_EVENTS.toString(), eventResponseDto.getEventCategory());
  }
}
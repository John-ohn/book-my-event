package com.BookMyEvent.service;

import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface EventService {
    public EventResponseDto createEvent(EventDTO eventDTO,
                                        MultipartFile firstImage,
                                        MultipartFile secondImage,
                                        MultipartFile thirdImage);

    EventResponseDto updateEvent(String eventId, EventDTO eventDTO);

    List<EventResponseDto> getApprovedEvents();

    void deleteEvent(String id);

    EventResponseDto getEventById(String eventId);

    EventResponseDto getApprovedEventById(String eventId);

    void deletePastEvents();

    List<EventResponseDto> getAllEvents();

    EventResponseDto updateEventStatus(String id, String status);

    EventResponseDto updateEventImage(String id, MultipartFile eventImage);

    List<EventResponseDto> getEventsByStatus(String status);

    EventResponseDto approveEvent(String id);

    //    EventDTO cancelEvent(String id);
    Map<String, Integer> countByStatus();

    void deleteNotLinkedImg();
}

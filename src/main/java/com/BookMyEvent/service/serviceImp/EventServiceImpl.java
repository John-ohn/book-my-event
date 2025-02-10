package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.ImageRepository;
import com.BookMyEvent.dao.UserRepository;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.mapper.EventMapper;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.EventService;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.MediaService;
import com.BookMyEvent.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final UserMapper userMapper;
    private final MediaService mediaService;
    private final MailService mailService;
    private final ImageRepository imageRepository;
    private final String className = this.getClass().getSimpleName();

    @Override
    @Transactional
    public EventResponseDto createEvent(EventDTO eventDTO,
                                        MultipartFile firstImage,
                                        MultipartFile secondImage,
                                        MultipartFile thirdImage) {
        log.info("{}}::createEvent - Creating new event with pending status: {}", className, eventDTO);
        if (eventDTO.getOrganizers().getId() != null) {
            List<Image> listImage = new ArrayList<>();
            try {
                List<MultipartFile> multipartFilesList = getMultipartFiles(firstImage, secondImage, thirdImage);
                listImage = mediaService.savedEventImg(multipartFilesList);
                String title= eventDTO.getTitle();
                AtomicInteger counter= new AtomicInteger(1);
                listImage.forEach(img->{img.setName(title+"/"+counter); counter.getAndIncrement();
                });
                log.info("{}}::createEvent - saved Img and return  List of Images.", className);
            } catch (Exception e) {
                log.error("{}::createEvent - Error processing image: {}", className, e.getMessage());
                throw new GeneralException("Error processing image: " + e.getMessage(), HttpStatus.PAYLOAD_TOO_LARGE);
            }
            Event event = eventMapper.toEvent(eventDTO);
            event.setCreationDate(LocalDateTime.now());
            event.setAvailableTickets(event.getNumberOfTickets());
            event.linkAllImageWithEvent(listImage);
            event.setEventStatus(EventStatus.PENDING);
            User user = userService.findUserById(eventDTO.getOrganizers().getId());
            event.linkUserWithEvent(user);
            Event savedEvent = eventRepository.save(event);

            mailService.sendSimpleHtmlMailMessage4Line(user.getEmail(),
                "Твоя подія успішно створена на BookMyEvent",
                "Вітаємо! Твоя подія успішно створена \uD83C\uDF89",
                String.format("Ми раді повідомити, що твоя подія [%s] успішно створена.", savedEvent.getTitle()),
                "Дякуємо, що обрав нашу платформу для організації подій. Якщо тобі потрібна додаткова" +
                    " допомога чи виникнуть запитання, звертайся до нашої служби підтримки.",
                "",
                ""
            );
            log.info("{}::createEvent - Event ({}) from user ({}) created successfully.", className, savedEvent.getTitle(), savedEvent.getOrganizers().getEmail());

            return eventMapper.toEventResponseDtoFromEvent(
                savedEvent,
                userMapper.toUserResponseDtoWithoutAvatarAndEvents(event.getOrganizers()));

        } else {
            throw new GeneralException("User ID can be null.", HttpStatus.BAD_REQUEST);
        }
    }

    @NotNull
    private static List<MultipartFile> getMultipartFiles(MultipartFile firstImage, MultipartFile secondImage, MultipartFile thirdImage) {
        List<MultipartFile> multipartFilesList = new ArrayList<>();
        if (firstImage != null && !firstImage.isEmpty()) {
            multipartFilesList.add(firstImage);
        }
        if (secondImage != null && !secondImage.isEmpty()) {
            multipartFilesList.add(secondImage);
        }
        if (thirdImage != null && !thirdImage.isEmpty()) {
            multipartFilesList.add(thirdImage);
        }
        return multipartFilesList;
    }

    @Override
    @Transactional
    public EventResponseDto approveEvent(String id) {
        log.info("EventServiceImpl::approveEvent - Approving event with ID: {}", id);
        Event existingEvent = eventRepository.findById(new ObjectId(id))
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        existingEvent.setEventStatus(EventStatus.APPROVED);
        Event approvedEvent = eventRepository.save(existingEvent);
        log.info("EventServiceImpl::approveEvent - Event approved successfully: {}", approvedEvent);

        return eventMapper.toEventResponseDtoFromEventWithoutUser(
            approvedEvent);
    }

    @Override
    @Transactional
    public EventResponseDto updateEvent(String id, EventDTO eventDTO) {
        log.info("EventServiceImpl::updateEvent - Updating event ID: {} with data: {}", id, eventDTO);
        Event existingEvent = eventRepository.findById(new ObjectId(id))
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        eventMapper.updateEventFromDTO(eventDTO, existingEvent);
        Event updatedEvent = eventRepository.save(existingEvent);
        log.info("EventServiceImpl::updateEvent - Event updated successfully: {}", updatedEvent);
        return eventMapper.toEventResponseDtoFromEvent(updatedEvent,
            userService.findUserInfoById(updatedEvent.getOrganizers().getId().toHexString()));
    }

    @Override
    @Transactional
    public EventResponseDto updateEventImage(String id, MultipartFile eventImage) {
        log.info("EventServiceImpl::updateEventImage - Updating Img event ID: {}", id);
        Event existingEvent = eventRepository.findById(new ObjectId(id))
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        Image newImage = mediaService.savedImg(eventImage);
        existingEvent.linkImageWithEvent(newImage);
        Event updatedEvent = eventRepository.save(existingEvent);
        log.info("EventServiceImpl::updateEvent - Event updated successfully: {}", updatedEvent);

        return eventMapper.toEventResponseDtoFromEvent(updatedEvent,
            userService.findUserInfoById(updatedEvent.getOrganizers().getId().toHexString()));
    }

//    @Override
//    @Transactional
//    public EventDTO cancelEvent(String id) {
//        log.info("EventServiceImpl::cancelEvent - Cancelling event with ID: {}", id);
//
//        Event existingEvent = eventRepository.findById(new ObjectId(id))
//            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
//
//        existingEvent.setEventStatus(EventStatus.CANCELLED);
//
//        Event cancelledEvent = eventRepository.save(existingEvent);
//
//        log.info("EventServiceImpl::cancelEvent - Event cancelled successfully: {}", cancelledEvent);
//        return eventMapper.toEventResponseDtoFromEvent(cancelledEvent,
//            cancelledEvent.getImages(),
//            userService.findUserInfoById(cancelledEvent.getOrganizers().getId().toHexString()));
//    }

    @Override
    public List<EventResponseDto> getApprovedEvents() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();

        List<Event> events = eventRepository.findEventByEventStatus(EventStatus.APPROVED);
        try {
            List<EventResponseDto> eventDTOs = events.stream()
                .map(eventMapper::toEventResponseDtoFromEventWithoutUser)
                .toList();

            log.info("{}::{} - Found {} events", className, methodName, events.size());
            return eventDTOs;
        } catch (Exception e) {

            log.info("{}}::{} - Exception  {} events", className, methodName, e.getMessage());
            throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public List<EventResponseDto> getAllEvents() {
        String methodName = new Object() {
        }.getClass().getEnclosingMethod().getName();
        log.info("{}::{} - Fetching all events", className, methodName);
        List<Event> events = eventRepository.findAll();
        try {
            List<EventResponseDto> eventDTOs = events.stream()
                .sorted((e1, e2) -> {
                    if (e1.getEventCategory() == EventCategory.TOP_EVENTS
                        && e2.getEventCategory() != EventCategory.TOP_EVENTS) {
                        return -1;
                    } else if (e1.getEventCategory() != EventCategory.TOP_EVENTS
                        && e2.getEventCategory() == EventCategory.TOP_EVENTS) {
                        return 1;
                    }
                    return e1.getCreationDate().compareTo(e2.getCreationDate());
                })
                .map(event -> {
                    return eventMapper.toEventResponseDtoFromEvent(event,
                        userMapper.toUserResponseDtoWithoutAvatarAndEvents(event.getOrganizers())
                    );
                })

                .toList();

            log.info("EventServiceImpl::getEvents - Found {} events", events.size());
            return eventDTOs;
        } catch (Exception e) {

            log.info("{}}::{} - Exception  {} events", this.getClass().getSimpleName(), methodName, e.getMessage());
            throw new GeneralException(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Map<String, Integer> countByStatus() {
        log.info("EventServiceImpl::countByStatus - Start counting events for all statuses");

        Map<String, Integer> statusCountMap = Arrays.stream(EventStatus.values())
            .collect(Collectors.toMap(
                Enum::toString,
                status -> eventRepository.findEventByEventStatus(status).size()
            ));

        log.info("EventServiceImpl::countByStatus - Events count by status: {}", statusCountMap);
        return statusCountMap;
    }


    @Override
    @Transactional
    public void deleteEvent(String id) {
        log.info("EventServiceImpl::deleteEvent - Deleting event ID: {}", id);
        Event event = eventRepository.findById(new ObjectId(id))
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        mediaService.deleteAll(event.getImages());
        eventRepository.delete(event);
        log.info("EventServiceImpl::deleteEvent - Event marked as deleted: {}", id);
    }

    @Override
    @Transactional
    public void deleteNotLinkedImg() {
        log.info("EventServiceImpl::deleteEvent - Deleting event img ");

        List<Event> event = eventRepository.findAll();
        List<Image> eventsImgs = event.stream().flatMap(
            event1 -> event1.getImages().stream()
        ).toList();
        List<Image> images = imageRepository.findAll();
        List<Image> imagesForDeleted = images.stream()
            .filter(img -> !eventsImgs.contains(img)).toList();
        List<String> idLs = imagesForDeleted.stream().map(img -> img.getId().toHexString()).toList();
        log.info("EventServiceImpl::deleteEvent - Img marked as deleted: size({}) imagesForDeleted : {}", imagesForDeleted.size(), idLs);

        if (imagesForDeleted.size() > 0) {
            mediaService.deleteAll(imagesForDeleted);
            log.info("EventServiceImpl::deleteEvent - Img marked as deleted:");
        }
    }

    @Override
    public EventResponseDto getEventById(String eventId) {
        log.info("EventServiceImpl::getEventById - Fetching event ID: {}", eventId);
        Event event = eventRepository.findById(new ObjectId(eventId))
            .orElseThrow(() -> new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND));

        EventResponseDto eventDTO = eventMapper.toEventResponseDtoFromEvent(event,
            userMapper.toUserResponseDto(event.getOrganizers())
        );
//        log.info("EventServiceImpl::getEventById - Found event: {}", eventDTO);
        return eventDTO;
    }

    @Override
    public EventResponseDto getApprovedEventById(String eventId) {
        log.info("EventServiceImpl::getEventById - Fetching event ID: {}", eventId);
        Event event = eventRepository.findById(new ObjectId(eventId))
            .orElseThrow(() -> new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND));
        if (event.getEventStatus().equals(EventStatus.APPROVED)) {
            EventResponseDto eventDTO = eventMapper.toEventResponseDtoFromEvent(event,
                userMapper.toUserResponseDto(event.getOrganizers())
            );
//        log.info("EventServiceImpl::getEventById - Found event: {}", eventDTO);
            return eventDTO;
        } else {
            throw new GeneralException("Event not found with ID " + eventId, HttpStatus.NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void deletePastEvents() {
        log.info("EventServiceImpl::deletePastEvents - Deleting past events...");
        LocalDate now = LocalDate.now().minusDays(1);
        List<Event> pastEvents = eventRepository.findByDateDay(now.toString());
//        LocalDateTime now = LocalDateTime.now();
//        List<Event> pastEvents = eventRepository.findByEndDateBefore(now);
        if (pastEvents.isEmpty()) {
            log.info("EventServiceImpl::deletePastEvents - No past events found for deletion.");
        } else {
            List<Image> imagesId = pastEvents.stream()
                .flatMap(event -> event.getImages().stream())
                .toList();
            mediaService.deleteAll(imagesId);
            eventRepository.deleteAll(pastEvents);
            log.info("EventServiceImpl::deletePastEvents - Deleted {} past events.", pastEvents.size());
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void scheduledDeletePastEvents() {
        log.info("EventServiceImpl::scheduledDeletePastEvents - Running scheduled task to delete past events");
        deletePastEvents();
    }

    @Override
    @Transactional
    public EventResponseDto updateEventStatus(String id, String status) {
        log.info("EventServiceImpl::updateEventStatus - Updating event ID: {} with new status: {}", id, status);

        Event existingEvent = eventRepository.findById(new ObjectId(id))
            .orElseThrow(() -> new GeneralException("Event not found with ID " + id, HttpStatus.NOT_FOUND));
        EventStatus newStatus = parseEventStatus(status);
        existingEvent.setEventStatus(newStatus);

        Event updatedEvent = eventRepository.save(existingEvent);

        log.info("EventServiceImpl::updateEventStatus - Event status updated successfully: {}", updatedEvent);

        return eventMapper.toEventResponseDtoFromEventWithoutUser(updatedEvent);
    }

    @Override
    public List<EventResponseDto> getEventsByStatus(String status) {
        log.info("Fetching events with status: {}", status);

        EventStatus eventStatus;
        try {
            eventStatus = EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new GeneralException("Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
        }
        List<Event> events = eventRepository.findEventByEventStatus(eventStatus);
        return events.stream()
            .map(eventMapper::toEventResponseDtoFromEventWithoutUser)
            .collect(Collectors.toList());
    }

    private EventStatus parseEventStatus(String status) {
        try {
            return EventStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.error("Invalid status value: {}", status, e);
            throw new GeneralException("Invalid status value or such status doesn't exist: " + status, HttpStatus.BAD_REQUEST);
        }
    }
}
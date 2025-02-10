package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.TestConfig;
import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.Location;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.mapper.EventMapper;
import com.BookMyEvent.mapper.UserMapper;
import com.BookMyEvent.service.MailService;
import com.BookMyEvent.service.MediaService;
import com.BookMyEvent.service.UserService;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.DoNotMock;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalTime;

import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@Import(TestConfig.class)
class EventServiceImplTest {

  @Mock
  private EventRepository eventRepository;

  @InjectMocks
  private EventServiceImpl eventService;

  @Mock
  private EventMapper eventMapper;
  @Mock
  private MediaService mediaService;
  @Mock
  private UserService userService;
  @Mock
  private MailService mailService;
  @Mock
  private UserMapper userMapper;

  private Event event;
  private DateDetails dateDetails;
  private EventResponseDto eventResponseDto;
  DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
  DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);

  @BeforeEach
  void setUp() {
    LocalTime localTime = LocalTime.now();
    List<Image> imageList = Arrays.asList(Image.builder()
        .id(new ObjectId("66c648b600179737a3d5c212")).build()
    );
    event = new Event();
    event.setId(new ObjectId("66c648b600179737a3d5c235"));
    event.setTitle("Test Event");
    event.setDescription("Test Description");
    event.setEventType(EventType.SPORTS_EVENTS);
    event.setEventCategory(EventCategory.TOP_EVENTS);
    event.setEventStatus(EventStatus.PENDING);
    event.setEventFormat(EventFormat.OFFLINE);
    event.setAvailableTickets(100);
    event.setNumberOfTickets(100);
    event.setUnlimitedTickets(false);
    event.setPhoneNumber("+380961232456");
    event.setTicketPrice(800L);
    event.setLocation(new Location(
        "Київ",
        "вул. Успішна, 1",
        "",
        "50.426129",
        "30.514067"));
    event.setAboutOrganizer("Text About Organizer");
    event.setRating(4.2D);
    event.setImages(imageList);
    event.setOrganizers(User.builder().id(new ObjectId("66c648b600179737a3d5c211"))
        .email("test@email.com")
        .createdEvents(new ArrayList<>())
        .build());
    event.setDate(new DateDetails(
        LocalDate.of(LocalDate.now().plusYears(1).getYear(),
            10, 21
        ).toString(),
        localTime.toString(),
        localTime.plusHours(2L).toString()));

    dateDetails = new DateDetails(LocalDate.parse(
        event.getDate().day()).format(dayFormatter),
        LocalTime.parse(event.getDate().time()).format(timeFormatter),
        LocalTime.parse(event.getDate().endTime()).format(timeFormatter));


    eventResponseDto = new EventResponseDto();
    eventResponseDto.setId(event.getId().toHexString());
    eventResponseDto.setTitle(event.getTitle());
    eventResponseDto.setDescription(event.getDescription());
    eventResponseDto.setEventType(event.getEventType().getUkrainianName());
    eventResponseDto.setEventCategory(event.getEventCategory().toString());
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    eventResponseDto.setEventFormat(event.getEventFormat().toString());
    eventResponseDto.setAvailableTickets(event.getAvailableTickets());
    eventResponseDto.setNumberOfTickets(event.getNumberOfTickets());
    eventResponseDto.setUnlimitedTickets(event.getUnlimitedTickets());
    eventResponseDto.setPhoneNumber(event.getPhoneNumber());
    eventResponseDto.setTicketPrice(event.getTicketPrice());
    eventResponseDto.setLocation(event.getLocation());
    eventResponseDto.setAboutOrganizer(event.getAboutOrganizer());
    eventResponseDto.setRating(event.getRating());
    eventResponseDto.setImages(event.getImages());
    eventResponseDto.setDate(event.getDate());

  }

  @Test
  @DisplayName("Test EventServiceImpl method getEvents")
  void testMethodGetEvents() {

    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getApprovedEvents();
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
        () -> assertNull(result.get(0).getOrganizers())
    );

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.APPROVED);
  }

  @Test
  void testUpdateEventStatus() {
    event.setEventStatus(EventStatus.APPROVED);
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    when(eventRepository.findById(event.getId())).thenReturn(Optional.of(event));
    when(eventRepository.save(event)).thenReturn(event);
    when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);

    EventResponseDto result = eventService.updateEventStatus(event.getId().toHexString(), EventStatus.APPROVED.toString());

    assertEquals(EventStatus.APPROVED.toString(), result.getEventStatus());
    verify(eventRepository, times(1)).findById(event.getId());
    verify(eventRepository, times(1)).save(event);
    verify(eventMapper, times(1)).toEventResponseDtoFromEventWithoutUser(event);

  }

  @Test
  @DisplayName("Test EventServiceImpl method GetEventsByStatus find PENDING events")
  void testMethodGetEventsByStatusPENDING() {
    event.setEventStatus(EventStatus.PENDING);
    when(eventRepository.findEventByEventStatus(EventStatus.PENDING)).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getEventsByStatus(EventStatus.PENDING.toString());
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
        () -> assertEquals(EventStatus.PENDING.toString(), result.get(0).getEventStatus())
    );

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.PENDING);
  }

  @Test
  @DisplayName("Test EventServiceImpl method GetEventsByStatus find APPROVED events")
  void testMethodGetEventsByStatusAPPROVED() {
    event.setEventStatus(EventStatus.APPROVED);
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);
    List<EventResponseDto> result = eventService.getEventsByStatus(EventStatus.APPROVED.toString());
    assertAll(
        () -> assertFalse(result.isEmpty()),
        () -> assertEquals(1, result.size()),
        () -> assertEquals(event.getId().toHexString(), result.get(0).getId()),
        () -> assertEquals(EventStatus.APPROVED.toString(), result.get(0).getEventStatus())
    );

    verify(eventRepository, times(1)).findEventByEventStatus(EventStatus.APPROVED);
  }

  @Test
  @DisplayName("Test EventServiceImpl method countByStatus.")
  void countByStatus(){

    when(eventRepository.findEventByEventStatus(EventStatus.APPROVED)).thenReturn(List.of(event));
    when(eventRepository.findEventByEventStatus(EventStatus.CANCELLED)).thenReturn(List.of());
    when(eventRepository.findEventByEventStatus(EventStatus.PENDING)).thenReturn(List.of(event));

    Map<String, Integer> countByStatus = eventService.countByStatus();
    assertAll(
        () -> assertFalse(countByStatus.isEmpty()),
        () -> assertEquals(1, countByStatus.get(EventStatus.APPROVED.toString())),
        () -> assertEquals(1, countByStatus.get(EventStatus.PENDING.toString())),
        () -> assertEquals(0, countByStatus.get(EventStatus.CANCELLED.toString()))
    );
  }

  @Test
  @DisplayName("Test EventServiceImpl method approveEvent.")
  void testMethodApproveEvent() {
    event.setEventStatus(EventStatus.APPROVED);
    eventResponseDto.setEventStatus(event.getEventStatus().toString());
    when(eventRepository.findById(event.getId())).thenReturn(Optional.ofNullable(event));
    when(eventRepository.save(event)).thenReturn(event);
    when(eventMapper.toEventResponseDtoFromEventWithoutUser(event)).thenReturn(eventResponseDto);

    EventResponseDto result = eventService.approveEvent(event.getId().toHexString());
    assertAll(
        () -> assertNotNull(result),
        () -> assertEquals(event.getId().toHexString(), result.getId()),
        () -> assertEquals(EventStatus.APPROVED.toString(), result.getEventStatus())
    );
  }

  @Test
  public void testCreateEvent() {

    EventDTO eventDTO = new EventDTO();
    eventDTO.setOrganizers(UserResponseDto.builder()
        .id(event.getOrganizers().getId().toHexString())
        .email(event.getOrganizers().getEmail()).build());

    MultipartFile firstImage = mock(MultipartFile.class);
    MultipartFile secondImage = mock(MultipartFile.class);
    MultipartFile thirdImage = mock(MultipartFile.class);

    UserResponseDto userResponseDto = new UserResponseDto();
    userResponseDto.setId(eventDTO.getOrganizers().getId());
    userResponseDto.setEmail(eventDTO.getOrganizers().getEmail());
    eventResponseDto.setOrganizers(userResponseDto);

    when(userService.findUserById(eventDTO.getOrganizers().getId())).thenReturn(event.getOrganizers());
    List<MultipartFile> multipartFiles = Arrays.asList(firstImage, secondImage, thirdImage);
    when(mediaService.savedEventImg(multipartFiles)).thenReturn(Collections.emptyList());
    when(eventMapper.toEvent(eventDTO)).thenReturn(event);
    when(eventRepository.save(event)).thenReturn(event);
    doNothing().when(mailService).sendSimpleHtmlMailMessage4Line(
        eventDTO.getOrganizers().getEmail(),
        "Твоя подія успішно створена на BookMyEvent",
        "Вітаємо! Твоя подія успішно створена \uD83C\uDF89",
        String.format("Ми раді повідомити, що твоя подія [%s] успішно створена.", event.getTitle()),
        "Дякуємо, що обрав нашу платформу для організації подій. Якщо тобі потрібна додаткова" +
            " допомога чи виникнуть запитання, звертайся до нашої служби підтримки.",
        "",
        "");
    when(eventMapper.toEventResponseDtoFromEvent(event, userResponseDto)).thenReturn(eventResponseDto);
    when(userMapper.toUserResponseDtoWithoutAvatarAndEvents(event.getOrganizers())).thenReturn(userResponseDto);

    EventResponseDto result = eventService.createEvent(eventDTO, firstImage, secondImage, thirdImage);

    assertNotNull(result);
    assertEquals(EventStatus.PENDING.toString(), result.getEventStatus());
    assertEquals(event.getId().toHexString(), result.getId());
    assertEquals(event.getOrganizers().getId().toHexString(), result.getOrganizers().getId());
    assertEquals(event.getImages().get(0).getId(), result.getImages().get(0).getId());

    verify(eventRepository, times(1)).save(any(Event.class));
  }
}
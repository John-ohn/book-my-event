package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventDTO;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import org.bson.types.ObjectId;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface EventMapper {

    //    @Mapping(target = "id", source = "eventDTO.id")
    @Mapping(target = "id", expression = "java(convertToObjectId(eventDTO.getId()))")
    @Mapping(target = "title", source = "eventDTO.title")
    @Mapping(target = "description", source = "eventDTO.description")
    @Mapping(target = "photoUrl", source = "eventDTO.photoUrl")
    @Mapping(target = "phoneNumber", source = "eventDTO.phoneNumber")
    @Mapping(target = "ticketPrice", source = "eventDTO.ticketPrice")
    @Mapping(target = "numberOfTickets", source = "eventDTO.numberOfTickets")
    @Mapping(target = "availableTickets", source = "eventDTO.availableTickets")
    @Mapping(target = "location", source = "eventDTO.location")
    @Mapping(target = "date", source = "eventDTO.date")
    @Mapping(target = "eventUrl", source = "eventDTO.eventUrl")
    @Mapping(target = "eventStatus", source = "eventDTO.eventStatus")
    @Mapping(target = "aboutOrganizer", source = "eventDTO.aboutOrganizer")
    @Mapping(target = "eventFormat", source = "eventDTO.eventFormat")
    @Mapping(target = "unlimitedTickets", source = "eventDTO.unlimitedTickets")
//    @Mapping(target = "userId", expression = "java(eventDTO.getOrganizers().getId() != null ? eventDTO.getOrganizers().getId(): null)")
    @Mapping(target = "images", ignore = true)
    Event toEvent(EventDTO eventDTO);

    @Mapping(target = "id", expression = "java(convertToStringId(event.getId()))")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "description", source = "event.description")
    @Mapping(target = "photoUrl", source = "event.photoUrl")
    @Mapping(target = "phoneNumber", source = "event.phoneNumber")
    @Mapping(target = "ticketPrice", source = "event.ticketPrice")
    @Mapping(target = "numberOfTickets", source = "event.numberOfTickets")
    @Mapping(target = "availableTickets", source = "event.availableTickets")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "organizers", source = "user")
    @Mapping(target = "eventUrl", source = "event.eventUrl")
    @Mapping(target = "eventStatus", source = "event.eventStatus")
    @Mapping(target = "creationDate", source = "event.creationDate")
    @Mapping(target = "images", ignore = true)
    EventDTO toEventDTO(Event event, UserResponseDto user);

//    @Mapping(target = "id", expression = "java(convertToStringId(event.getId()))")
//    @Mapping(target = "title", source = "event.title")
//    @Mapping(target = "description", source = "event.description")
//    @Mapping(target = "photoUrl", source = "event.photoUrl")
//    @Mapping(target = "phoneNumber", source = "event.phoneNumber")
//    @Mapping(target = "ticketPrice", source = "event.ticketPrice")
//    @Mapping(target = "numberOfTickets", source = "event.numberOfTickets")
//    @Mapping(target = "availableTickets", source = "event.availableTickets")
//    @Mapping(target = "location", source = "event.location")
//    @Mapping(target = "eventUrl", source = "event.eventUrl")
//    @Mapping(target = "eventStatus", source = "event.eventStatus")
//    @Mapping(target = "creationDate", source = "event.creationDate")
//    @Mapping(target = "images", source = "event.images")
//    @Mapping(target = "organizers", source = "user")
//    EventDTO toEventDTOWithImages(Event event, UserResponseDto user);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "creationDate", ignore = true)
    @Mapping(target = "images", ignore = true)
    void updateEventFromDTO(EventDTO eventDTO, @MappingTarget Event event);

    @Mapping(target = "id", expression = "java(convertToStringId(event.getId()))")
    @Mapping(target = "title", source = "event.title")
    @Mapping(target = "description", source = "event.description")
    @Mapping(target = "photoUrl", source = "event.photoUrl")
    @Mapping(target = "phoneNumber", source = "event.phoneNumber")
    @Mapping(target = "ticketPrice", source = "event.ticketPrice")
    @Mapping(target = "numberOfTickets", source = "event.numberOfTickets")
    @Mapping(target = "availableTickets", source = "event.availableTickets")
    @Mapping(target = "location", source = "event.location")
    @Mapping(target = "eventUrl", source = "event.eventUrl")
    @Mapping(target = "eventStatus", source = "event.eventStatus")
    @Mapping(target = "creationDate", source = "event.creationDate")
    @Mapping(target = "eventType", expression = "java(event.getEventType() != null ? event.getEventType().getUkrainianName() : null)")
    @Mapping(target = "eventCategory", expression = "java(event.getEventCategory() != null ? event.getEventCategory().toString(): null)")
    @Mapping(target = "images", source = "event.images")
    @Mapping(target = "organizers", expression = "java(user != null ? user: null)")
    EventResponseDto toEventResponseDtoFromEvent(Event event,
                                                 UserResponseDto user);

    @Mapping(target = "id", expression = "java(convertToStringId(event.getId()))")
//    @Mapping(target = "title", source = "event.title")
//    @Mapping(target = "description", source = "event.description")
//    @Mapping(target = "photoUrl", source = "event.photoUrl")
//    @Mapping(target = "phoneNumber", source = "event.phoneNumber")
//    @Mapping(target = "ticketPrice", source = "event.ticketPrice")
//    @Mapping(target = "numberOfTickets", source = "event.numberOfTickets")
//    @Mapping(target = "availableTickets", source = "event.availableTickets")
//    @Mapping(target = "location", source = "event.location")
//    @Mapping(target = "eventUrl", source = "event.eventUrl")
    //    @Mapping(target = "creationDate", source = "event.creationDate")
    @Mapping(target = "eventStatus", expression = "java(convertEventStatusToString(event))")
    @Mapping(target = "eventFormat", expression = "java(convertEventFormatToString(event))")
    @Mapping(target = "eventType", expression = "java(convertEventTypeToString(event))")
    @Mapping(target = "eventCategory", expression = "java(convertEventCategoryToString(event))")
//    @Mapping(target = "images", source = "event.images")
    @Mapping(target = "organizers", ignore = true)
    EventResponseDto toEventResponseDtoFromEventWithoutUser(Event event);

    default ObjectId convertToObjectId(String eventId) {
        return eventId != null && eventId.isEmpty() ? new ObjectId(eventId) : new ObjectId();
    }

    default String convertToStringId(ObjectId eventId) {
        return eventId != null ? eventId.toHexString() : null;
    }

    default String convertEventCategoryToString(Event event) {
        return event.getEventCategory() != null ? event.getEventCategory().toString(): null;
    }

    default String convertEventTypeToString(Event event) {
        return event.getEventType() != null ? event.getEventType().getUkrainianName() : null;
    }

    default String convertEventFormatToString(Event event) {
        return event.getEventFormat() != null ? event.getEventFormat().toString(): null;
    }

    default String convertEventStatusToString(Event event) {
        return event.getEventStatus() != null ? event.getEventStatus().toString(): null;
    }

//    default  DateDetails formatDate(DateDetails date) {
//        if (date == null) {
//            return null;
//        }
//
//        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("d MMMM", new Locale("uk"));
//        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.ENGLISH);
//
//        String formattedDay = LocalDate.parse(date.day()).format(dayFormatter);
//        String formattedTime = date.time() != null ? LocalTime.parse(date.time()).format(timeFormatter) : null;
//        String formattedEndTime = date.endTime() != null ? LocalTime.parse(date.endTime()).format(timeFormatter) : null;
//        return new DateDetails(formattedDay, formattedTime, formattedEndTime);
//    }
}

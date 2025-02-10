package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketResponseDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface TicketMapper {

  TicketMapper INSTANCE = Mappers.getMapper(TicketMapper.class);

  @Mapping(target = "buyingDate", expression = "java(java.time.LocalDateTime.now())")
  @Mapping(target = "reservationExpires", expression = "java(java.time.LocalDateTime.now().plusMinutes(15))")
  @Mapping(target = "eventId", source = "eventId", qualifiedByName = "stringToObjectId") // Изменено
  @Mapping(target = "userId", source = "userId", qualifiedByName = "stringToObjectId") // Изменено
  Ticket toTicket(String eventId, String userId, int numberOfTickets, Long row, Long seat);

  @Mapping(source = "ticket.id", target = "id")
  @Mapping(source = "ticket.title", target = "title")
  @Mapping(source = "ticket.description", target = "description")
  @Mapping(target = "eventId", expression = "java(ticket.getEventId() != null ? ticket.getEventId().toHexString() : null)")
  @Mapping(target = "userId", expression = "java(ticket.getUserId() != null ? ticket.getUserId().toHexString() : null)")
  @Mapping(source = "ticket.startDate", target = "startDate")
  @Mapping(source = "ticket.startTime", target = "startTime")
  @Mapping(source = "ticket.buyingDate", target = "buyingDate")
  @Mapping(source = "ticket.reservationExpires", target = "reservationExpires")
  @Mapping(source = "ticket.row", target = "row")
  @Mapping(source = "ticket.seat", target = "seat")
  @Mapping(source = "ticket.ticketPrice", target = "ticketPrice")
  @Mapping(source = "ticket.numberOfTickets", target = "numberOfTickets")
  @Mapping(source = "ticket.location", target = "location")
  TicketResponseDto toUserResponseDto(Ticket ticket);

  @Named("stringToObjectId")
  default ObjectId mapStringToObjectId(String id) {
    return (id != null && !id.isEmpty()) ? new ObjectId(id) : null;
  }
}

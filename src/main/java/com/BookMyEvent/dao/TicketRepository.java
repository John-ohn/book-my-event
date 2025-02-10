package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Ticket;
import com.BookMyEvent.entity.dto.TicketResponseDto;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends MongoRepository<Ticket, String> {

  List<TicketResponseDto> findAllByEventId(ObjectId eventId);

  List<TicketResponseDto> findAllByUserId(ObjectId userId);

//  @Query(value = "{ 'eventId': ?0 }", count = true)
//  Optional<Long> countByEventId(ObjectId eventId);

//  boolean existsByEventIdAndRowAndSeat(ObjectId eventId, Long row, Long seat);

  @Query(value = "{ 'eventId': ?0 }", count = true)
  Optional<Long> countByEventId(String eventId);

  boolean existsByEventIdAndRowAndSeat(String eventId, Long row, Long seat);
  @Query("{ 'eventId': ?0, 'userId': ?1, 'reservationExpires': { $gt: ?2 } }")
  List<Ticket> findByEventIdAndUserIdAndReservationExpiresAfter(String eventId, String userId, LocalDateTime dateTime);
  List<Ticket> findByReservationExpiresBefore(LocalDateTime dateTime);

  Optional<Ticket> findByEventIdAndUserId(String eventId, String userId);

  List<Ticket> findByEventIdAndRowAndSeat(String eventId, Long row, Long seat);
}

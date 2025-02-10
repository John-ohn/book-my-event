package com.BookMyEvent.dao;

import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends MongoRepository<Event, ObjectId> {

  @Query(value = "{ 'id': ?0 }", fields = "{ 'numberOfTickets': 1}")
  Optional<Event> findEventNumberOfTickets(String id);

  List<Event> findByDateDay(String day);

  @Query("{ 'date.day' : ?0 }")
  List<Event> findByEventStartDate(String day);
  List<Event> findEventByEventStatus(EventStatus eventStatus);



}

package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Document(collection = "tickets")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {


  @Id
  private String id;
  private String title;
  private String description;
  private ObjectId eventId;
  private ObjectId userId;
  private LocalDate startDate;
  private LocalDateTime reservationExpires;
  private LocalTime startTime;
  private LocalDateTime buyingDate;
  private Long row;
  private Long seat;
  private int numberOfTickets;

  private String ticketPrice;
  private String location;
}

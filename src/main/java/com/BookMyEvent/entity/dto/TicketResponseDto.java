package com.BookMyEvent.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TicketResponseDto {

  private String id;
  private String title;
  private String description;
  private String eventId;
  private String userId;
  private LocalDate startDate;
  private LocalTime startTime;
  private LocalDateTime buyingDate;
  private Long row;
  private LocalDateTime reservationExpires;
  private Long seat;
  private int numberOfTickets;
  private String ticketPrice;
  private String location;
}

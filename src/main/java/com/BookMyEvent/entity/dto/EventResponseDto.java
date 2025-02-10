package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.entity.Location;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {

  private String id;
  private String title;
  private String description;
  private String photoUrl; // deleted
  @JsonProperty("date")
  private DateDetails date;
  private LocalDateTime creationDate;
  private Integer availableTickets;
  private Boolean unlimitedTickets;
  private String phoneNumber;
  @JsonProperty("price")
  private Long ticketPrice;
  private Integer numberOfTickets;
  private Location location;
  private UserResponseDto organizers;
  private String aboutOrganizer;
  private double rating;
  @JsonProperty("type")
  private String eventType;
  @JsonProperty("category")
  private String eventCategory;
  private String eventFormat;
  private String eventUrl;
  private String eventStatus;
  private List<Image> images;
}

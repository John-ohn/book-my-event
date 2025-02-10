package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.EventCategory;
import com.BookMyEvent.entity.Enums.EventFormat;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Enums.EventType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {

  @Id
  private ObjectId id;
  private String title;
  private String description;
  private String photoUrl;
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
  //  private String userId;
  private String aboutOrganizer;
  private double rating;
  @JsonProperty("type")
  private EventType eventType;
  @JsonProperty("category")
  private EventCategory eventCategory;
  private EventFormat eventFormat;
  private EventStatus eventStatus = EventStatus.PENDING;
  private String eventUrl;
  @DBRef(lazy = false)
  private List<Image> images=new ArrayList<>();
  @DBRef
  private User organizers;

  public void linkUserWithEvent(User user) {
    user.getCreatedEvents().add(this);
    this.setOrganizers(user);
  }

  public void linkImageWithEvent(Image image) {
    this.getImages().add(image);
  }

  public void linkAllImageWithEvent(List<Image> image) {
    this.getImages().addAll(image);
  }
}

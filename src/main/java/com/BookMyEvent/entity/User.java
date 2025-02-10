package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.Role;
import com.BookMyEvent.entity.Enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class User {

  @Id
  private ObjectId id;
  private String name;
  private String surname;
  @Indexed(unique = true)
  private String email;
  private String password;
  private boolean mailConfirmation;
  private Role role;
  private LocalDate birthdayDate;
  private LocalDateTime creationDate;
  private String phoneNumber;
  @DBRef
  private Image avatarImage;
  private String location;
  private Status status;
  @ToString.Exclude
  @DBRef
  private List<Event> createdEvents = new ArrayList<>();

  public void linkImageWithUser(Image image) {
    this.setAvatarImage(image);
  }
}

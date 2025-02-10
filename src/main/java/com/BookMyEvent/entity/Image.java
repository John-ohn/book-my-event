package com.BookMyEvent.entity;


import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Image {
  @Id
  @JsonSerialize(using = ToStringSerializer.class)
  private ObjectId id;
  private String name;
  private byte[] photoInBytes;
  private LocalDateTime creationDate;
  private boolean isMain;
}

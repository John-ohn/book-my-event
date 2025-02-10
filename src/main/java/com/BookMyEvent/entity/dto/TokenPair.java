package com.BookMyEvent.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor
public class TokenPair {

  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ssa")
  private LocalDateTime timestamp = LocalDateTime.now();

  private String userId;
  private String userName;
  private String accessToken;


  public TokenPair(String userId, String userName, String accessToken) {
    this.userId = userId;
    this.userName = userName;
    this.accessToken = accessToken;
    this.timestamp = LocalDateTime.now();
  }

}

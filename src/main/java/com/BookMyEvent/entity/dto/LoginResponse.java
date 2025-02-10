package com.BookMyEvent.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class LoginResponse {

//  @Builder.Default
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ssa")
  private LocalDateTime timestamp ;

  private String userId;
  private String userName;
  private String accessToken;
  private String message;
  @JsonProperty("status")
  private Integer statusCode;


  public LoginResponse(String userId, String userName, String accessToken, String message, Integer statusCode) {
    this.userId = userId;
    this.userName = userName;
    this.accessToken = accessToken;
    this.message = message;
    this.statusCode = statusCode;
    this.timestamp = LocalDateTime.now();
  }

  public LoginResponse(String userId, String userName, String message, Integer statusCode) {
    this.userId = userId;
    this.userName = userName;
    this.message = message;
    this.statusCode = statusCode;
    this.timestamp = LocalDateTime.now();
  }

  public LoginResponse(String message, Integer statusCode) {
    this.message = message;
    this.statusCode = statusCode;
    this.timestamp = LocalDateTime.now();
  }
}

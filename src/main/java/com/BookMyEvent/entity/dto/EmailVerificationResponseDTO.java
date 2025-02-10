package com.BookMyEvent.entity.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class EmailVerificationResponseDTO {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ssa")
  private LocalDateTime timestamp;

  @JsonProperty("status")
  private Integer statusCode;

  private String message;

  private Boolean emailExist;
  public EmailVerificationResponseDTO(Integer statusCode, String message, Boolean emailExist) {
    this.timestamp = LocalDateTime.now();
    this.statusCode = statusCode;
    this.message = message;
    this.emailExist = emailExist;
  }
}

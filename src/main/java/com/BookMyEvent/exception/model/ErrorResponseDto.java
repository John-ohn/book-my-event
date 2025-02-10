package com.BookMyEvent.exception.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ErrorResponseDto {

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ssa")
  private LocalDateTime timestamp;

  @JsonProperty("status")
  private Integer statusCode;

  private String message;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, String> details;

  public ErrorResponseDto(Integer statusCode, String message){
    this.timestamp=LocalDateTime.now();
    this.statusCode=statusCode;
    this.message=message;
    this.details=null;
  }

  public ErrorResponseDto(Integer statusCode, String message, Map<String, String> details) {
    this.timestamp = LocalDateTime.now();
    this.statusCode = statusCode;
    this.message = message;
    this.details = details;
  }

}

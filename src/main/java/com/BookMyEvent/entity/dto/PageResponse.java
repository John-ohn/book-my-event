package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class PageResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd hh:mm:ssa")
    private LocalDateTime timestamp;

    @JsonProperty("status")
    private Integer statusCode;

    private  List<UserResponseDto> users;

    public PageResponse(Integer statusCode, List<UserResponseDto> users) {
        this.timestamp = LocalDateTime.now();
        this.statusCode = statusCode;
        this.users = users;
    }
}

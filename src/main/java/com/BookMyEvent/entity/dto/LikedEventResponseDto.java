package com.BookMyEvent.entity.dto;

import com.BookMyEvent.entity.DateDetails;
import com.BookMyEvent.entity.Event;

import java.util.List;

public record LikedEventResponseDto( String userId, List<Event> eventsList) {
}
package com.BookMyEvent.entity.dto;

import java.time.LocalDateTime;

public record LikedEventDto(String userId, String eventId, LocalDateTime createdAt, LocalDateTime updatedAt) {
}
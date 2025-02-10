package com.BookMyEvent.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "user_liked_events")
public class UserLikedEvent {
    @Id
    private String id;
    private String userId;
    private String eventId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


package com.BookMyEvent.service;

import com.BookMyEvent.entity.dto.LikedEventDto;
import com.BookMyEvent.entity.dto.LikedEventResponseDto;

import java.util.List;

public interface UserLikedEventService {
    void addLikedEvent(LikedEventDto likedEventDto);
    void removeLikedEvent(LikedEventDto likedEventDto);
    LikedEventResponseDto getLikedEvents(String userId);
    long countLikedEvents(String userId);
    Long getTotalEventLikes(String eventId);
}
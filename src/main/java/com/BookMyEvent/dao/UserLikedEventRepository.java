package com.BookMyEvent.dao;

import com.BookMyEvent.entity.UserLikedEvent;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserLikedEventRepository extends MongoRepository<UserLikedEvent, String> {
    List<UserLikedEvent> findByUserId(String userId);
    boolean existsByUserIdAndEventId(String userId, String eventId);
    void deleteByUserIdAndEventId(String userId, String eventId);
    long countByUserId(String userId);
    Optional<UserLikedEvent> findByUserIdAndEventId(String userId, String eventId);
    Long countByEventId(String eventId);
}
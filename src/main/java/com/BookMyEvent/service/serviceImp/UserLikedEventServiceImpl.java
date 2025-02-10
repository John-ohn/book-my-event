package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.EventRepository;
import com.BookMyEvent.dao.UserLikedEventRepository;
import com.BookMyEvent.entity.Enums.EventStatus;
import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.UserLikedEvent;
import com.BookMyEvent.entity.dto.LikedEventDto;
import com.BookMyEvent.entity.dto.LikedEventResponseDto;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.UserLikedEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserLikedEventServiceImpl implements UserLikedEventService {
  private final UserLikedEventRepository likedEventRepository;
  private final EventRepository eventRepository;
  private final Map<String, Long> likedEventCountCache = new ConcurrentHashMap<>();

  @Override
  public long countLikedEvents(String userId) {
    log.info("UserLikedEventServiceImpl::countLikedEvents - Counting liked events for userId: {}", userId);

    if (likedEventCountCache.containsKey(userId)) {
      return likedEventCountCache.get(userId);
    }

    long count = likedEventRepository.countByUserId(userId);
    likedEventCountCache.put(userId, count);
    return count;
  }

  @Override
  public void addLikedEvent(LikedEventDto likedEventDto) {
    if (likedEventDto.userId() == null || likedEventDto.eventId() == null) {
      throw new GeneralException("UserId and EventId must not be null", HttpStatus.BAD_REQUEST);
    }

    log.info("UserLikedEventServiceImpl::addLikedEvent - Adding liked event for userId: {}, eventId: {}",
        likedEventDto.userId(), likedEventDto.eventId());

    if (likedEventRepository.existsByUserIdAndEventId(likedEventDto.userId(), likedEventDto.eventId())) {
      log.warn("UserLikedEventServiceImpl::addLikedEvent - Event already liked by user: {}", likedEventDto.userId());
      throw new GeneralException("Event already liked", HttpStatus.CONFLICT);
    }

    UserLikedEvent likedEvent = new UserLikedEvent();
    likedEvent.setUserId(likedEventDto.userId());
    likedEvent.setEventId(likedEventDto.eventId());
    likedEvent.setCreatedAt(LocalDateTime.now());
    likedEvent.setUpdatedAt(LocalDateTime.now());

    likedEventRepository.save(likedEvent);
    log.info("UserLikedEventServiceImpl::addLikedEvent - Successfully added liked event for userId: {}",
        likedEventDto.userId());

    likedEventCountCache.merge(likedEventDto.userId(), 1L, Long::sum);
  }

  @Override
  public void removeLikedEvent(LikedEventDto likedEventDto) {
    log.info("UserLikedEventServiceImpl::removeLikedEvent - Removing liked event for userId: {}, eventId: {}",
        likedEventDto.userId(), likedEventDto.eventId());

    if (!likedEventRepository.existsByUserIdAndEventId(likedEventDto.userId(), likedEventDto.eventId())) {
      log.warn("UserLikedEventServiceImpl::removeLikedEvent - Event not found in liked events for user: {}",
          likedEventDto.userId());
      throw new GeneralException("Event not found in liked events", HttpStatus.NOT_FOUND);
    }

    likedEventRepository.deleteByUserIdAndEventId(likedEventDto.userId(), likedEventDto.eventId());
    log.info("UserLikedEventServiceImpl::removeLikedEvent - Successfully removed liked event for userId: {}",
        likedEventDto.userId());

    likedEventCountCache.merge(likedEventDto.userId(), -1L, Long::sum);
  }

  @Override
  public LikedEventResponseDto getLikedEvents(String userId) {
    log.info("UserLikedEventServiceImpl::getLikedEvents - Fetching liked events for userId: {}", userId);

    List<UserLikedEvent> likedEvents = likedEventRepository.findByUserId(userId);

    if (likedEvents.isEmpty()) {
      log.info("UserLikedEventServiceImpl::getLikedEvents - No liked events found for userId: {}", userId);
      return new LikedEventResponseDto(
          userId,
          Collections.emptyList()
      );
    }

    log.info("Fetched {} liked events for userId: {}", likedEvents.size(), userId);

    List<Event> eventsList = likedEvents.stream()
        .map(likedEvent -> eventRepository.findById(new ObjectId(likedEvent.getEventId())))
        .filter(Optional::isPresent)
        .map(Optional::get)
        .toList();

    return new LikedEventResponseDto(
        userId,
        eventsList
    );
  }

  @Override
  public Long getTotalEventLikes(String eventId) {
    Long totalEventLikes = likedEventRepository.countByEventId(eventId);
    log.info("Fetched {} liked events for eventId: {}", totalEventLikes, eventId);
    return totalEventLikes;
  }
}
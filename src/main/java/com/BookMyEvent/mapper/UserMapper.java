package com.BookMyEvent.mapper;

import com.BookMyEvent.entity.Event;
import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.EventResponseDto;
import com.BookMyEvent.entity.dto.UserResponseDto;
import com.BookMyEvent.entity.dto.UserSaveDto;
import com.BookMyEvent.entity.dto.UserUpdateDto;
import org.bson.types.ObjectId;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

import static org.mapstruct.InjectionStrategy.CONSTRUCTOR;
import static org.mapstruct.NullValuePropertyMappingStrategy.IGNORE;

@Mapper(componentModel = "spring",
    injectionStrategy = CONSTRUCTOR,
    nullValuePropertyMappingStrategy = IGNORE)
public interface UserMapper {

  @Mapping(target = "id", expression = "java(convertToStringId(user.getId()))")
//  @Mapping(source = "user.email", target = "email")
//  @Mapping(source = "user.creationDate", target = "creationDate")
//  @Mapping(source = "user.location", target = "location")
//  @Mapping(source = "user.status", target = "status")
  @Mapping(target = "createdEvents", ignore = true)
  UserResponseDto toUserResponseDto(User user);

  @Mapping(target = "id", expression = "java(convertToStringId(user.getId()))")
  @Mapping(target = "avatarImage", ignore = true)
  @Mapping(target = "createdEvents", ignore = true)
  UserResponseDto toUserResponseDtoWithoutAvatarAndEvents(User user);

//  @Mapping(source = "userSaveDto.name", target = "name")
//  @Mapping(source = "userSaveDto.email", target = "email")
//  @Mapping(source = "userSaveDto.password", target = "password")
//  @Mapping(source = "userSaveDto.phone", target = "phone")
  User toUserFromUserSaveDto(UserSaveDto userSaveDto);

  @Mapping(target = "id", ignore = true)
  @Mapping(target = "password", ignore = true)
  void mapUserUpdateToUser(UserUpdateDto userUpdate, @MappingTarget User user);

  @Mapping(target = "id", expression = "java(convertToStringId(event.getId()))")
  @Mapping(target = "organizers", ignore = true)
  EventResponseDto toEventResponseDto(Event event);

  List<EventResponseDto> toEventDTOList(List<Event> events);

  @Mapping(target = "createdEvents", source = "createdEvents")
  UserResponseDto toUserResponseDtoWithEvents(User user);

  default ObjectId convertToObjectId(String eventId) {
    return eventId != null && eventId.isEmpty() ? new ObjectId(eventId) : new ObjectId();
  }

  default String convertToStringId(ObjectId eventId) {
    return eventId != null ? eventId.toHexString() : null;
  }
}

package com.BookMyEvent.dao;

import com.BookMyEvent.entity.User;
import com.BookMyEvent.entity.dto.UserResponseDto;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, ObjectId> {

  @Query(value = "{ 'email': ?0 }", fields = "{ 'password': 0 }")
  Optional<UserResponseDto> findUserInfoByEmail(String userEmail);

  @Query(value = "{ 'id': ?0 }", fields = "{ 'password': 0 }")
  Optional<UserResponseDto> findUserInfoById(String id);

  @Query(value = "{}", fields = "{ 'password': 0 }")
  List<UserResponseDto> findAllUserProfiles();

  Optional<User> findUserByEmail(String userEmail);

  @NotNull Page<User> findAll(@NotNull Pageable pageable);

  boolean existsByEmail(String email);

}

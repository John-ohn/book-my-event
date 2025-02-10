package com.BookMyEvent.dao;

import com.BookMyEvent.entity.DeletedUsers;
import com.BookMyEvent.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface DeletedUsersRepository extends MongoRepository<DeletedUsers, String> {

 boolean existsByEmail(String userEmail);
}

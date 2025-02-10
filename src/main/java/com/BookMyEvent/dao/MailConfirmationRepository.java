package com.BookMyEvent.dao;

import com.BookMyEvent.entity.UserEmailData;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MailConfirmationRepository extends MongoRepository<UserEmailData, String> {
    @Query(value = "{ 'email': ?0 }", fields = "{ 'id': 1, 'email': 1, 'emailCode': 1}")
    Optional<UserEmailData> findByEmail(String userEmail);

}


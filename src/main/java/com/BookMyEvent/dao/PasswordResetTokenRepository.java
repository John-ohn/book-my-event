package com.BookMyEvent.dao;

import com.BookMyEvent.entity.PasswordResetToken;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasswordResetTokenRepository extends MongoRepository<PasswordResetToken, String> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByToken(String token);

    Optional<PasswordResetToken> findByUserId(String userId);

    boolean existsByUserId(String userId);
}
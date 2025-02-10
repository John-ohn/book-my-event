package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "password_reset_tokens")
public class PasswordResetToken {

    @Id
    private String id;
    private String token;
    private String userId;
    private LocalDateTime expirationTime;

    public PasswordResetToken(String userId) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.expirationTime = LocalDateTime.now().plusHours(1);
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }
}
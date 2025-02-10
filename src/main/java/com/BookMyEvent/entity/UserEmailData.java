package com.BookMyEvent.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "user_email_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserEmailData {
    public UserEmailData(String email, String emailCode) {
        this.email = email;
        this.emailCode = emailCode;
    }

    @Id
    private String id;
    private String email,emailCode;

   }

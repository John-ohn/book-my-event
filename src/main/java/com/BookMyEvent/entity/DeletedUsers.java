package com.BookMyEvent.entity;

import com.BookMyEvent.entity.Enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "deleted_users")
@Data
@NoArgsConstructor
public class DeletedUsers{

    @Id
    private ObjectId id;

    @Indexed(unique = true)
    private String email;
    private LocalDateTime creationDate=LocalDateTime.now();

    public DeletedUsers(String email){
        this.email = email;
        creationDate=LocalDateTime.now();
    }

}

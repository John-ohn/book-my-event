package com.BookMyEvent.dao;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import com.BookMyEvent.entity.Image;

import java.awt.*;

@Repository
public interface ImageRepository extends MongoRepository<Image, ObjectId> {
}

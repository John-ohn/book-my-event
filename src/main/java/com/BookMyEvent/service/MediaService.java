package com.BookMyEvent.service;

import com.BookMyEvent.entity.Image;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface MediaService {
    List<byte[]> getImageBytes(List<MultipartFile> multipartFiles) throws IOException;

//    String getImageBytesAsBase64(MultipartFile file) throws IOException;

    byte[] getSingleImageBytes(MultipartFile file) throws IOException;

//    List<Image> getEventImagesById(List<String> listImageId);

    List<Image> savedEventImg(List<MultipartFile> images);
    Image savedImg(MultipartFile image);

    String updateImageById(MultipartFile image, String imageId);

//    void deleteEventImg(List<String> imagesId);
//
//    void deleteAllEventImg(List<String> imagesId);

    void deleteAll(List<Image> imagesId);
}
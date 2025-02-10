package com.BookMyEvent.service.serviceImp;

import com.BookMyEvent.dao.ImageRepository;
import com.BookMyEvent.entity.Image;
import com.BookMyEvent.exception.GeneralException;
import com.BookMyEvent.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.bson.types.ObjectId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MediaServiceImp implements MediaService {
    private final ImageRepository imageRepository;
    private final String clasName = this.getClass().getSimpleName();
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of
        ("image/jpeg", "image/webp", "image/svg+xml", "image/png");
    final static long MAX_SIZE_MB = 1 * 1024 * 1024;

    @Override
    public List<byte[]> getImageBytes(List<MultipartFile> multipartFiles) throws IOException {
        List<byte[]> imgByBytes = new ArrayList<>();

        for (MultipartFile file : multipartFiles) {
            validateFile(file);
            if ("image/svg+xml".equals(file.getContentType())) {
                imgByBytes.add(file.getBytes());
                continue;
            }
            byte[] optimizedImage = optimizeImage(file);
            imgByBytes.add(optimizedImage);
        }
        return imgByBytes;
    }

    private void validateFile(MultipartFile file) throws IOException {
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new IOException("Unsupported file type: " + file.getContentType());
        }

        if (file.getSize() > MAX_SIZE_MB) {
            throw new IOException("File size exceeds the 1MB limit: " + file.getOriginalFilename());
        }
    }

    private byte[] optimizeImage(MultipartFile file) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(file.getInputStream())
            .size(1024, 768)
            .outputQuality(0.7)
            .toOutputStream(outputStream);
        byte[] optimizedImage = outputStream.toByteArray();
        if (optimizedImage.length > MAX_SIZE_MB) {
            throw new GeneralException("Optimized image still exceeds the 1MB limit: " + file.getOriginalFilename(), HttpStatus.PAYLOAD_TOO_LARGE);
        }

        return optimizedImage;
    }

//    @Override
//    public String getImageBytesAsBase64(MultipartFile file) throws IOException {
//        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
//            throw new GeneralException("Unsupported file type: " + file.getContentType(), HttpStatus.BAD_REQUEST);
//        }
//
//        return Base64.getEncoder().encodeToString(file.getBytes());
//    }

    @Override
    public byte[] getSingleImageBytes(MultipartFile file) throws IOException {
        validateFile(file);
        if ("image/svg+xml".equals(file.getContentType())) {
            return file.getBytes();
        }
        byte[] optimizedImage = optimizeImage(file);
        return optimizedImage;
    }

//    @Override
//    public List<Image> getEventImagesById(List<String> listImageId) {
//        if (listImageId != null && !listImageId.isEmpty()) {
//            List<Image> eventImages = listImageId.stream()
//                .map(imageId -> {
//                    Image image = imageRepository.findById(new ObjectId(imageId))
//                        .orElseThrow(() -> new GeneralException(
//                            String.format("Image with ID [%s] not found.", imageId),
//                            HttpStatus.NOT_FOUND));
//                    return image;
//                }).toList();
//            log.info("{}::getEventImagesById. Return all existing Event Images.", clasName);
//            return eventImages;
//        }
//        return new ArrayList<>();
//    }

    @Override
    public List<Image> savedEventImg(List<MultipartFile> images) {
        List<Image> existingImages = new ArrayList<>();
        try {
            boolean main=true;
            for (MultipartFile imageFile : images) {
                byte[] optimizedImage = getSingleImageBytes(imageFile);

                Image newImage = Image.builder()
                    .photoInBytes(optimizedImage)
                    .creationDate(LocalDateTime.now())
                    .isMain(main)
                    .build();
                Image saveImage = imageRepository.save(newImage);
                main=false;
                log.info("{}::savedEventImg. Return all existing Event Images. bytes - {} ", clasName, saveImage.getId());
                existingImages.add(saveImage);
            }
            return existingImages;
        } catch (IOException e) {
            throw new GeneralException("Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public Image savedImg(MultipartFile image) {
        try {
                byte[] optimizedImage = getSingleImageBytes(image);
                Image newImage = Image.builder()
                    .photoInBytes(optimizedImage)
                    .creationDate(LocalDateTime.now())
                    .isMain(true)
                    .build();
                Image saveImage = imageRepository.save(newImage);
                log.info("{}::savedEventImg. Return all existing Event Images. Id - {} ", clasName, saveImage.getId());

            return saveImage;
        } catch (IOException e) {
            throw new GeneralException("Failed to process image files: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public String updateImageById(MultipartFile image, String imageId) {
        var imageOptional = imageRepository.findById(new ObjectId(imageId)).orElseThrow(
            () -> new GeneralException("Image not found with ID: " + imageId, HttpStatus.NOT_FOUND));
        try {
            byte[] optimizedImageBytes = getSingleImageBytes(image);

            if (optimizedImageBytes == null || optimizedImageBytes.length == 0) {
                throw new GeneralException("Failed to optimize the uploaded image. No valid image data found.", HttpStatus.BAD_REQUEST);
            }
            imageOptional.setPhotoInBytes(optimizedImageBytes);
            imageRepository.save(imageOptional);
        } catch (IOException e) {
            throw new GeneralException("Failed to process image file: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        return "Image updated successfully";
    }

//    @Override
//    public void deleteEventImg(List<String> imagesId) {
//        for (String imageId : imagesId) {
//            imageRepository.deleteById(imageId);
//            log.info("{}::deleteEventImg. deleteById existing Event Images. {} ", clasName, imageId);
//        }
//    }

//    @Override
//    public void deleteAllEventImg(List<String> imagesId) {
//            imageRepository.deleteAllById(imagesId);
//            log.info("{}::deleteEventImg. delete all existing Event Images by id. ", clasName);
//    }

    @Override
    public void deleteAll(List<Image> imagesId) {
        if(!imagesId.isEmpty()){
        imageRepository.deleteAll(imagesId);
        log.info("{}::deleteAll. delete all existing Event Images by id. ", clasName);
        }
    }
}
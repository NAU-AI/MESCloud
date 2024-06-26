package com.alcegory.mescloud.azure.service;

import com.alcegory.mescloud.azure.model.AnnotationEntity;
import com.alcegory.mescloud.azure.model.ImageAnnotationEntity;
import com.alcegory.mescloud.azure.model.constant.Status;
import com.alcegory.mescloud.azure.model.converter.ImageAnnotationConverter;
import com.alcegory.mescloud.azure.model.dto.ImageAnnotationDto;
import com.alcegory.mescloud.azure.repository.AnnotationRepository;
import com.alcegory.mescloud.azure.repository.ImageAnnotationRepository;
import com.alcegory.mescloud.exception.ImageAnnotationException;
import com.alcegory.mescloud.model.entity.UserEntity;
import com.alcegory.mescloud.repository.UserRepository;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

import static com.alcegory.mescloud.azure.model.constant.Status.*;

@Service
@AllArgsConstructor
@Log
public class ImageAnnotationServiceImpl implements ImageAnnotationService {

    private final ImageAnnotationRepository imageAnnotationRepository;
    private final AnnotationRepository annotationRepository;
    private final UserRepository userRepository;
    private final ImageAnnotationConverter converter;

    @Override
    public void saveImageAnnotation(ImageAnnotationDto imageAnnotationDto, Authentication authentication) {
        if (imageAnnotationDto == null) {
            throw new IllegalArgumentException("ImageAnnotationDto cannot be null");
        }

        ImageAnnotationEntity imageAnnotation = converter.dtoToEntity(imageAnnotationDto);
        imageAnnotation.setRegisteredAt(new Date());
        imageAnnotation.setUser(getUser(authentication));

        setStatusAndSaveImageWithTransaction(imageAnnotation, INITIAL);
    }

    public void saveApprovedImageAnnotation(ImageAnnotationDto imageAnnotationDto, boolean isApproved,
                                            Authentication authentication) {
        if (imageAnnotationDto == null) {
            throw new IllegalArgumentException("ImageAnnotationDto cannot be null");
        }

        ImageAnnotationEntity imageAnnotation = converter.dtoToEntity(imageAnnotationDto);
        imageAnnotation.setUser(getUser(authentication));
        imageAnnotation.setRegisteredAt(new Date());

        Status status = isApproved ? APPROVED : REJECTED;
        setStatusAndSaveImageWithTransaction(imageAnnotation, status);
    }

    @Override
    public boolean existsByUserIdAndImage(Long userId, String image) {
        if (userId == null || image == null) {
            return false;
        }
        return imageAnnotationRepository.existsByUserIdAndImage(userId, image);
    }

    @Override
    public int countByImage(String image) {
        if (image == null) {
            return 0;
        }
        return imageAnnotationRepository.countByImage(image);
    }

    @Override
    public int countByImageAndStatusNotInitial(String image) {
        if (image == null) {
            return 0;
        }
        return imageAnnotationRepository.countByImageAndStatusNotInitial(image);
    }

    private void setStatusAndSaveImageWithTransaction(ImageAnnotationEntity imageAnnotation, Status status) {
        imageAnnotation.setStatus(status);
        saveImageWithTransaction(imageAnnotation);
    }

    public void saveImageWithTransaction(ImageAnnotationEntity imageAnnotation) {
        if (imageAnnotation == null) {
            throw new IllegalArgumentException("ImageAnnotationEntity cannot be null");
        }

        try {
            ImageAnnotationEntity persistedImageAnnotation = imageAnnotationRepository.save(imageAnnotation);

            List<AnnotationEntity> annotationEntities = persistedImageAnnotation.getAnnotations();
            if (annotationEntities != null && !annotationEntities.isEmpty()) {
                for (AnnotationEntity annotation : annotationEntities) {
                    annotation.setImageAnnotation(persistedImageAnnotation);
                }
                annotationRepository.saveAll(annotationEntities);
            }
        } catch (Exception e) {
            throw new ImageAnnotationException("Failed to save image annotation", e);
        }
    }

    private UserEntity getUser(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalArgumentException("Authentication cannot be null");
        }

        String username = authentication.getName();

        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        return userRepository.findUserByUsername(username);
    }
}

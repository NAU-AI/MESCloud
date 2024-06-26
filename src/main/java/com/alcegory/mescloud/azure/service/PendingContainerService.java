package com.alcegory.mescloud.azure.service;

import com.alcegory.mescloud.azure.model.dto.ContainerInfoDto;
import com.alcegory.mescloud.azure.model.dto.ImageAnnotationDto;
import com.alcegory.mescloud.azure.model.dto.ImageInfoDto;

import java.util.List;

public interface PendingContainerService {

    List<ContainerInfoDto> getPendingImageAnnotations();

    ContainerInfoDto getImageAnnotationDtoByImageInfo(ImageInfoDto imageInfoDto);

    ImageAnnotationDto getImageAnnotationFromContainer(String imageUrl);

    void deleteJpgAndJsonBlobs(String blobName);
}

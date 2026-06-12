package org.example.project_webservice_it211.dto.response;

import lombok.Data;
import org.example.project_webservice_it211.entity.CourtImage;

import java.time.LocalDateTime;

@Data
public class CourtImageResponse {
    private Long id;
    private String imageUrl;
    private String fileName;
    private Integer displayOrder;
    private LocalDateTime uploadedAt;

    public static CourtImageResponse from(CourtImage image) {
        CourtImageResponse dto = new CourtImageResponse();
        dto.setId(image.getId());
        dto.setImageUrl(image.getImageUrl());
        dto.setFileName(image.getFileName());
        dto.setDisplayOrder(image.getDisplayOrder());
        dto.setUploadedAt(image.getUploadedAt());
        return dto;
    }
}

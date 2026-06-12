package org.example.project_webservice_it211.dto.response;

import lombok.Data;
import org.example.project_webservice_it211.entity.Court;

import java.util.Collections;
import java.util.List;

@Data
public class CourtResponse {
    private Long id;
    private String courtName;
    private String type;
    private String imageUrl;
    private Boolean isAvailable;
    private Long clusterId;
    private String clusterName;
    private List<CourtImageResponse> images;

    public static CourtResponse from(Court court) {
        CourtResponse dto = new CourtResponse();
        dto.setId(court.getId());
        dto.setCourtName(court.getCourtName());
        dto.setType(court.getType());
        dto.setImageUrl(court.getImageUrl());
        dto.setIsAvailable(court.getIsAvailable());

        if (court.getCluster() != null) {
            dto.setClusterId(court.getCluster().getId());
            dto.setClusterName(court.getCluster().getName());
        }

        if (court.getImages() != null) {
            dto.setImages(court.getImages().stream()
                    .map(CourtImageResponse::from)
                    .toList());
        } else {
            dto.setImages(Collections.emptyList());
        }

        return dto;
    }
}

package org.example.project_webservice_it211.controller;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.response.ApiResponse;
import org.example.project_webservice_it211.dto.response.CourtImageResponse;
import org.example.project_webservice_it211.service.CourtImageService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/manager/courts/{courtId}/images")
@RequiredArgsConstructor
public class CourtImageController {

    private final CourtImageService courtImageService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<List<CourtImageResponse>>> uploadImages(
            @PathVariable Long courtId,
            @RequestPart("files") List<MultipartFile> files,
            Authentication auth
    ) {
        List<CourtImageResponse> data = courtImageService.uploadImages(courtId, files, auth.getName());
        return ResponseEntity.status(201)
                .body(ApiResponse.created("Tải ảnh lên thành công (" + data.size() + " ảnh)", data));
    }

}

package org.example.project_webservice_it211.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project_webservice_it211.exception.CloudStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    @Value("${cloudinary.mock-mode:false}")
    private boolean mockMode;


    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) {
        if (mockMode) {
            return mockUpload(file);
        }

        try {
            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", "courts",
                            "resource_type", "image"
                    )
            );
            String url = (String) uploadResult.get("secure_url");
            if (url == null || url.isBlank()) {
                throw new CloudStorageException("Cloudinary không trả về URL hợp lệ");
            }
            log.info("[Cloudinary] Uploaded: {}", url);
            return url;

        } catch (IOException e) {
            log.error("[Cloudinary] Upload failed: {}", e.getMessage(), e);
            throw new CloudStorageException(
                    "Dịch vụ lưu trữ ảnh tạm thời không khả dụng. Vui lòng thử lại sau.", e
            );
        }
    }





    private String mockUpload(MultipartFile file) {
        String fakeId  = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        String ext     = getExtension(file.getOriginalFilename());
        String fakeUrl = "https://res.cloudinary.com/mock/image/upload/courts/" + fakeId + ext;
        log.info("[Cloudinary MOCK] Simulated upload → {}", fakeUrl);
        return fakeUrl;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return ".jpg";
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String extractPublicId(String imageUrl) {
        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) return imageUrl;

        String afterUpload = imageUrl.substring(uploadIndex + 8);

        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }

        int dotIndex = afterUpload.lastIndexOf('.');
        if (dotIndex != -1) {
            afterUpload = afterUpload.substring(0, dotIndex);
        }

        return afterUpload;
    }
}

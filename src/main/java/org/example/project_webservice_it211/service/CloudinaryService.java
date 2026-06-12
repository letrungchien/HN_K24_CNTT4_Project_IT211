package org.example.project_webservice_it211.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.project_webservice_it211.exception.CloudStorageException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

/**
 * Dịch vụ upload ảnh lên Cloudinary.
 * Không lưu file xuống local disk — kiến trúc Stateless.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload một file ảnh lên Cloudinary, lưu vào folder "courts".
     *
     * @param file MultipartFile từ request
     * @return public URL của ảnh sau khi upload
     * @throws CloudStorageException nếu Cloudinary không phản hồi hoặc upload thất bại
     */
    @SuppressWarnings("unchecked")
    public String uploadImage(MultipartFile file) {
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
            log.info("Uploaded image to Cloudinary: {}", url);
            return url;

        } catch (IOException e) {
            log.error("Cloudinary upload failed: {}", e.getMessage(), e);
            throw new CloudStorageException(
                    "Dịch vụ lưu trữ ảnh tạm thời không khả dụng. Vui lòng thử lại sau.", e
            );
        }
    }

    /**
     * Xoá ảnh trên Cloudinary theo publicId.
     * publicId được trích xuất từ URL (phần path sau /upload/).
     *
     * @param imageUrl URL đầy đủ của ảnh trên Cloudinary
     */
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;
        try {
            String publicId = extractPublicId(imageUrl);
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            log.info("Deleted image from Cloudinary: {}", publicId);
        } catch (IOException e) {
            // Không ném exception khi xóa thất bại — chỉ log warning
            log.warn("Could not delete image from Cloudinary ({}): {}", imageUrl, e.getMessage());
        }
    }

    /**
     * Trích xuất publicId từ Cloudinary URL.
     * Ví dụ: https://res.cloudinary.com/demo/image/upload/v123456/courts/abc.jpg
     *        → publicId = "courts/abc"
     */
    private String extractPublicId(String imageUrl) {
        // Lấy phần sau "/upload/" và bỏ phiên bản (v123456/) nếu có
        int uploadIndex = imageUrl.indexOf("/upload/");
        if (uploadIndex == -1) return imageUrl;

        String afterUpload = imageUrl.substring(uploadIndex + 8); // bỏ "/upload/"

        // Bỏ version prefix nếu có dạng "v1234567890/"
        if (afterUpload.matches("v\\d+/.*")) {
            afterUpload = afterUpload.substring(afterUpload.indexOf('/') + 1);
        }

        // Bỏ extension file
        int dotIndex = afterUpload.lastIndexOf('.');
        if (dotIndex != -1) {
            afterUpload = afterUpload.substring(0, dotIndex);
        }

        return afterUpload;
    }
}

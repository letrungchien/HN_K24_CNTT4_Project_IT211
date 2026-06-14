package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.dto.response.CourtImageResponse;
import org.example.project_webservice_it211.entity.Court;
import org.example.project_webservice_it211.entity.CourtImage;
import org.example.project_webservice_it211.exception.NotFoundException;
import org.example.project_webservice_it211.repository.CourtImageRepository;
import org.example.project_webservice_it211.repository.CourtRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CourtImageService {

    private final CourtRepository courtRepository;
    private final CourtImageRepository courtImageRepository;
    private final ManagerService managerService;

    private final CloudinaryService cloudinaryService;

    // ---------- Upload ----------

    @Transactional
    public List<CourtImageResponse> uploadImages(Long courtId, List<MultipartFile> files, String username) {

        Court court = getCourtOwnedByManager(courtId, username);

        // Validate tất cả file trước khi upload
        for (MultipartFile file : files) {
            validateImageFile(file);
        }

        int nextOrder = courtImageRepository.findMaxDisplayOrderByCourtId(courtId) + 1;

        List<CourtImageResponse> results = new ArrayList<>();

        for (MultipartFile file : files) {
            // Upload lên Cloudinary — nếu lỗi sẽ ném CloudStorageException → 503
            String imageUrl = cloudinaryService.uploadImage(file);

            CourtImage image = CourtImage.builder()
                    .fileName(file.getOriginalFilename() != null
                            ? file.getOriginalFilename()
                            : "image")
                    .imageUrl(imageUrl)                 // URL từ Cloudinary
                    .displayOrder(nextOrder++)
                    .uploadedAt(LocalDateTime.now())
                    .court(court)
                    .build();

            results.add(CourtImageResponse.from(courtImageRepository.save(image)));
        }

        syncCourtThumbnail(court);

        return results;
    }



    @Transactional
    public List<CourtImageResponse> reorderImages(Long courtId, List<Long> orderedImageIds, String username) {
        getCourtOwnedByManager(courtId, username);

        List<CourtImage> images = courtImageRepository.findByCourtIdOrderByDisplayOrderAsc(courtId);

        if (orderedImageIds.size() != images.size()) {
            throw new RuntimeException("Số lượng ảnh không khớp");
        }

        for (int i = 0; i < orderedImageIds.size(); i++) {
            Long imgId = orderedImageIds.get(i);
            CourtImage img = images.stream()
                    .filter(x -> x.getId().equals(imgId))
                    .findFirst()
                    .orElseThrow(() -> new NotFoundException("Ảnh id " + imgId + " không thuộc sân này"));
            img.setDisplayOrder(i);
            courtImageRepository.save(img);
        }

        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new NotFoundException("Sân không tồn tại"));
        syncCourtThumbnail(court);

        return courtImageRepository.findByCourtIdOrderByDisplayOrderAsc(courtId)
                .stream()
                .map(CourtImageResponse::from)
                .toList();
    }



    private Court getCourtOwnedByManager(Long courtId, String username) {
        Court court = courtRepository.findById(courtId)
                .orElseThrow(() -> new NotFoundException("Sân không tồn tại"));

        if (court.getCluster() == null
                || court.getCluster().getManager() == null
                || !court.getCluster().getManager().getUsername().equals(username)) {
            throw new RuntimeException("Bạn không có quyền quản lý sân này");
        }
        return court;
    }

    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new RuntimeException("File không được rỗng");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Chỉ chấp nhận file ảnh (jpg, png, gif, webp...)");
        }

        long maxSize = 5 * 1024 * 1024L;
        if (file.getSize() > maxSize) {
            throw new RuntimeException("Kích thước ảnh tối đa 5MB, file '"
                    + file.getOriginalFilename() + "' vượt giới hạn");
        }
    }

    private void syncCourtThumbnail(Court court) {
        courtImageRepository.findByCourtIdOrderByDisplayOrderAsc(court.getId())
                .stream()
                .findFirst()
                .ifPresentOrElse(
                        first -> {
                            court.setImageUrl(first.getImageUrl());
                            courtRepository.save(court);
                        },
                        () -> {
                            court.setImageUrl(null);
                            courtRepository.save(court);
                        }
                );
    }
}

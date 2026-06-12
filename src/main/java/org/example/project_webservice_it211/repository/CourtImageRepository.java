package org.example.project_webservice_it211.repository;

import org.example.project_webservice_it211.entity.CourtImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourtImageRepository extends JpaRepository<CourtImage, Long> {

    List<CourtImage> findByCourtIdOrderByDisplayOrderAsc(Long courtId);

    @Query("SELECT COALESCE(MAX(ci.displayOrder), -1) FROM CourtImage ci WHERE ci.court.id = :courtId")
    int findMaxDisplayOrderByCourtId(@Param("courtId") Long courtId);
}

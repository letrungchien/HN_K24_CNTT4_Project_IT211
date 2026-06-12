package org.example.project_webservice_it211.repository;

import org.example.project_webservice_it211.entity.Court;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourtRepository extends JpaRepository<Court,Long> {
}

package org.example.project_webservice_it211.repository;

import org.example.project_webservice_it211.entity.BadmintonCluster;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BadmintonClusterRepository extends JpaRepository<BadmintonCluster, Long> {

    List<BadmintonCluster> findByManagerUsername(String username);
}

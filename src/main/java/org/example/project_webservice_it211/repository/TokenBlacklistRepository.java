package org.example.project_webservice_it211.repository;

import org.example.project_webservice_it211.entity.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, Long> {
    boolean existsByToken(String token);
}

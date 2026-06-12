package org.example.project_webservice_it211.service;

import lombok.RequiredArgsConstructor;
import org.example.project_webservice_it211.entity.TokenBlacklist;
import org.example.project_webservice_it211.repository.TokenBlacklistRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BlacklistService {

    private final TokenBlacklistRepository repo;

    public void add(String authHeader) {

        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        TokenBlacklist bl = new TokenBlacklist();
        bl.setToken(token);
        repo.save(bl);
    }

    public boolean isBlacklisted(String token) {
        return repo.existsByToken(token);
    }
}

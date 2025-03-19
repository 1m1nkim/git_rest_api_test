package com.git_rest_api.github.controller;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RepositoryController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/api/repos")
    public ResponseEntity<Map<String, Object>> getRepos(
            @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient) {
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String username = authorizedClient.getPrincipalName();

        String cacheKey = "user_repos:" + username;

        @SuppressWarnings("unchecked")
        List<Map<String, String>> cachedRepositories = (List<Map<String, String>>) redisTemplate.opsForValue().get(cacheKey);

        Map<String, Object> response = new HashMap<>();

        if (cachedRepositories != null) {
            response.put("repositories", cachedRepositories);
            response.put("fromCache", true);
        } else {
            try {
                GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
                List<Map<String, String>> repositories = new ArrayList<>();

                for (GHRepository repo : github.getMyself().listRepositories()) {
                    if (repo.getOwnerName().equals(github.getMyself().getLogin())) {
                        Map<String, String> repoInfo = new HashMap<>();
                        repoInfo.put("name", repo.getName());
                        repoInfo.put("owner", repo.getOwnerName());
                        repositories.add(repoInfo);
                    }
                }

                redisTemplate.opsForValue().set(cacheKey, repositories, 10, TimeUnit.MINUTES);

                response.put("repositories", repositories);
                response.put("fromCache", false);

                return ResponseEntity.ok(response);
            } catch (IOException e) {
                response.put("error", "GitHub API 연동 오류: " + e.getMessage());
                return ResponseEntity.status(500).body(response);
            }
        }

        return ResponseEntity.ok(response);
    }
}

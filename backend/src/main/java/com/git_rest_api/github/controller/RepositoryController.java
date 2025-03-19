package com.git_rest_api.github.controller;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class RepositoryController {

    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/repos")
    public String viewRepos(Model model,
                            @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient) {
        // 올바른 방식으로 액세스 토큰 획득
        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String username = authorizedClient.getPrincipalName();

        // 캐시 키 생성
        String cacheKey = "user_repos:" + username;

        // Redis에서 캐시된 데이터 확인
        @SuppressWarnings("unchecked")
        List<Map<String, String>> cachedRepositories = (List<Map<String, String>>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedRepositories != null) {
            // 캐시된 데이터가 있으면 사용
            model.addAttribute("repositories", cachedRepositories);
            model.addAttribute("fromCache", true);
        } else {
            // 캐시된 데이터가 없으면 GitHub API 호출
            try {
                GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
                List<Map<String, String>> repositories = new ArrayList<>();

                // 사용자가 소유한 레포지토리만 필터링
                for (GHRepository repo : github.getMyself().listRepositories()) {
                    if (repo.getOwnerName().equals(github.getMyself().getLogin())) {
                        Map<String, String> repoInfo = new HashMap<>();
                        repoInfo.put("name", repo.getName());
                        repoInfo.put("owner", repo.getOwnerName());
                        repositories.add(repoInfo);
                    }
                }

                // 결과를 Redis에 저장
                redisTemplate.opsForValue().set(cacheKey, repositories, 10, TimeUnit.MINUTES);

                model.addAttribute("repositories", repositories);
                model.addAttribute("fromCache", false);
            } catch (IOException e) {
                model.addAttribute("error", "GitHub API 연동 오류: " + e.getMessage());
            }
        }
        return "repos";
    }
}

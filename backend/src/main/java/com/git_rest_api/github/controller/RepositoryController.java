package com.git_rest_api.github.controller;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
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

@Controller
@RequiredArgsConstructor
public class RepositoryController {

    @GetMapping("/repos")
    public String viewRepos(Model model,
                            @RegisteredOAuth2AuthorizedClient("github") OAuth2AuthorizedClient authorizedClient) {
        // 올바른 방식으로 액세스 토큰 획득
        String accessToken = authorizedClient.getAccessToken().getTokenValue();

        try {
            GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
            List<Map<String, String>> repositories = new ArrayList<>();

            // 수정된 부분: listRepositories()에서 직접 필터링
            for (GHRepository repo : github.getMyself().listRepositories()) {
                // 사용자가 소유한 레포지토리만 필터링
                if (repo.getOwnerName().equals(github.getMyself().getLogin())) {
                    Map<String, String> repoInfo = new HashMap<>();
                    repoInfo.put("name", repo.getName());
                    repoInfo.put("owner", repo.getOwnerName());
                    repositories.add(repoInfo);
                }
            }

            model.addAttribute("repositories", repositories);
        } catch (IOException e) {
            model.addAttribute("error", "GitHub API 연동 오류: " + e.getMessage());
        }
        return "repos";
    }

}
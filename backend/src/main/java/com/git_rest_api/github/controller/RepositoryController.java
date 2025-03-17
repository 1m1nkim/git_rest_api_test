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
import java.util.List;

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
            List<String> repoNames = new ArrayList<>();
            for (GHRepository repo : github.getMyself().getRepositories().values()) {
                repoNames.add(repo.getName());
            }
            model.addAttribute("repos", repoNames);
        } catch (IOException e) {
            model.addAttribute("error", "GitHub API 연동 오류: " + e.getMessage());
        }
        return "repos";  // src/main/resources/templates/repos.html
    }
}
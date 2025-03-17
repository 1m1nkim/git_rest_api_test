package com.git_rest_api.github.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/github")
public class GithubController {

    @GetMapping
    public String home(@AuthenticationPrincipal OAuth2User principal, Model model) {
        if (principal != null) {
            // GitHub OAuth 응답에서 "login" 속성을 통해 사용자 이름 획득
            String username = principal.getAttribute("login");
            model.addAttribute("username", username);
        }
        return "index";  // src/main/resources/templates/index.html
    }
}


//    @GetMapping("/repos")
//    public List<String> getRepos(@AuthenticationPrincipal OAuth2User principal) {
//        String accessToken = ((DefaultOAuth2User) principal).getAttribute("access_token");
//        try {
//            GitHub github = new GitHubBuilder().withOAuthToken(accessToken).build();
//            List<String> repoNames = new ArrayList<>();
//            for (GHRepository repo : github.getMyself().getRepositories().values()) {
//                repoNames.add(repo.getName());
//            }
//            return repoNames;
//        } catch (IOException e) {
//            throw new RuntimeException("GitHub API 연동 오류", e);
//        }
//    }
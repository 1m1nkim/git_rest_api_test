package com.git_rest_api.github.service;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.PagedIterable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GithubService {
    private final GitHub github;
    private final RestTemplate restTemplate;
    private final String githubToken;

    public GithubService(GitHub github, RestTemplate restTemplate, @Value("${github.token}") String githubToken) {
        this.github = github;
        this.restTemplate = restTemplate;
        this.githubToken = githubToken;
    }

    public List<GHCommit> getRecentCommits(String owner, String repoName, int page, int perPage) {
        try {
            GHRepository repository = github.getRepository(owner + "/" + repoName);

            // 효율적인 페이지네이션 처리
            PagedIterable<GHCommit> pagedCommits = repository.listCommits().withPageSize(perPage);
            List<GHCommit> commits = new ArrayList<>();

            // 페이지네이션 처리 개선
            int i = 0;
            for (GHCommit commit : pagedCommits) {
                if (i >= (page-1)*perPage && i < page*perPage) {
                    commits.add(commit);
                }
                if (i >= page*perPage) break;
                i++;
            }

            return commits;
        } catch (IOException e) {
            throw new RuntimeException("Error while fetching GitHub API", e);
        }
    }

    // 나머지 메서드는 그대로 유지
    public GHCommit getCommitDetail(String owner, String repoName, String sha) throws IOException {
        GHRepository repository = github.getRepository(owner + "/" + repoName);
        return repository.getCommit(sha);
    }

    public String getFileDiff(String owner, String repoName, String sha, String filePath) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3.diff");
        headers.set("Authorization", "Bearer " + githubToken);

        String url = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repoName, sha);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        String fullDiff = response.getBody();
        return extractFileDiff(fullDiff, filePath);
    }

    private String extractFileDiff(String fullDiff, String filePath) {
        String[] diffParts = fullDiff.split("diff --git");

        for (String part : diffParts) {
            if (part.contains(filePath)) {
                return "diff --git" + part;
            }
        }

        return "No changes found for this file.";
    }

    public Map<String, String> parseFileDiff(String diff) {
        Map<String, String> result = new HashMap<>();

        StringBuilder oldContent = new StringBuilder();
        StringBuilder newContent = new StringBuilder();

        String[] lines = diff.split("\n");
        for (String line : lines) {
            if (line.startsWith("-") && !line.startsWith("---")) {
                oldContent.append(line.substring(1)).append("\n");
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                newContent.append(line.substring(1)).append("\n");
            } else if (!line.startsWith("diff") && !line.startsWith("index") &&
                    !line.startsWith("---") && !line.startsWith("+++")) {
                oldContent.append(line).append("\n");
                newContent.append(line).append("\n");
            }
        }

        result.put("oldContent", oldContent.toString());
        result.put("newContent", newContent.toString());

        return result;
    }

}

package com.git_rest_api.github.service;

import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
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
            return repository.listCommits().withPageSize(perPage).asList().subList((page-1)*perPage,
                    Math.min(page*perPage, repository.listCommits().asList().size()));
        } catch (IOException e) {
            throw new RuntimeException("GitHub API 호출 중 오류 발생", e);
        }
    }

    public GHCommit getCommitDetail(String owner, String repoName, String sha) throws IOException {
        GHRepository repository = github.getRepository(owner + "/" + repoName);
        return repository.getCommit(sha);
    }

    public String getFileDiff(String owner, String repoName, String sha, String filePath) throws IOException {
        // GitHub API에서 diff 형식으로 데이터 요청
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", "application/vnd.github.v3.diff");
        // 인증 토큰 추가
        headers.set("Authorization", "Bearer " + githubToken);

        String url = String.format("https://api.github.com/repos/%s/%s/commits/%s", owner, repoName, sha);

        ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        // 전체 diff에서 특정 파일에 대한 diff만 추출
        String fullDiff = response.getBody();
        return extractFileDiff(fullDiff, filePath);
    }

    private String extractFileDiff(String fullDiff, String filePath) {
        // diff 파싱 로직 구현
        String[] diffParts = fullDiff.split("diff --git");

        for (String part : diffParts) {
            if (part.contains(filePath)) {
                return "diff --git" + part;
            }
        }

        return "해당 파일의 변경 내역을 찾을 수 없습니다.";
    }

    public Map<String, String> parseFileDiff(String diff) {
        Map<String, String> result = new HashMap<>();

        StringBuilder before = new StringBuilder();
        StringBuilder after = new StringBuilder();

        String[] lines = diff.split("\n");
        for (String line : lines) {
            if (line.startsWith("-") && !line.startsWith("---")) {
                before.append(line.substring(1)).append("\n");
            } else if (line.startsWith("+") && !line.startsWith("+++")) {
                after.append(line.substring(1)).append("\n");
            } else if (!line.startsWith("diff") && !line.startsWith("index") &&
                    !line.startsWith("---") && !line.startsWith("+++")) {
                before.append(line).append("\n");
                after.append(line).append("\n");
            }
        }

        result.put("before", before.toString());
        result.put("after", after.toString());

        return result;
    }
}

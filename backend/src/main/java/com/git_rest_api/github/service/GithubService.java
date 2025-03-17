package com.git_rest_api.github.service;

import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHRepository;
import org.kohsuke.github.GitHub;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GithubService {
    private final GitHub github;

    public List<GHCommit> getRecentCommits(String repoName) {
        try {
            GHRepository repository = github.getRepository(repoName);
            return repository.listCommits().asList();
        } catch (IOException e) {
            throw new RuntimeException("GitHub API 호출 중 오류 발생", e);
        }
    }

    public String createOrUpdateFile(String repoName, String path, String content, String commitMessage) {
        try {
            GHRepository repository = github.getRepository(repoName);
            repository.createContent(content, commitMessage, path);
            return "파일이 성공적으로 생성/수정되었습니다.";
        } catch (IOException e) {
            throw new RuntimeException("GitHub API 호출 중 오류 발생", e);
        }
    }

    public String getMyName() throws IOException {
        return github.getMyself().getName();
    }
}

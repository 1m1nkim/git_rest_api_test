package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommitDiffRestController {

    private final GithubService githubService;

    @GetMapping("/api/repos/{owner}/{repo}/commit/{sha}/file")
    public Map<String, String> getFileDiff(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String sha,
            @RequestParam String filePath) throws IOException {

        String diff = githubService.getFileDiff(owner, repo, sha, filePath);
        return githubService.parseFileDiff(diff);
    }
}

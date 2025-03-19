package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommitDetailController {

    private final GithubService githubService;
    private final RedisTemplate<String, Object> redisTemplate;

    // URL 경로를 commit에서 commits로 변경 (복수형)
    @GetMapping("/api/repos/{owner}/{repo}/commits/{sha}")
    public ResponseEntity<Map<String, Object>> getCommitDetail(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String sha) {
        try {
            String cacheKey = "commit_detail:" + owner + ":" + repo + ":" + sha;

            @SuppressWarnings("unchecked")
            Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return ResponseEntity.ok(cachedData);
            } else {
                GHCommit commit = githubService.getCommitDetail(owner, repo, sha);
                List<File> changedFiles = commit.getFiles();

                Map<String, Object> dataToCache = new HashMap<>();
                Map<String, Object> simplifiedCommit = new HashMap<>();
                simplifiedCommit.put("sha", commit.getSHA1());
                simplifiedCommit.put("authorName", commit.getAuthor().getName());
                simplifiedCommit.put("authorEmail", commit.getAuthor().getEmail());
                simplifiedCommit.put("commitDate", commit.getCommitDate());
                simplifiedCommit.put("message", commit.getCommitShortInfo().getMessage());

                List<Map<String, Object>> simplifiedFiles = new ArrayList<>();
                for (File file : changedFiles) {
                    Map<String, Object> fileInfo = new HashMap<>();
                    fileInfo.put("fileName", file.getFileName());
                    fileInfo.put("patch", file.getPatch());
                    fileInfo.put("additions", file.getLinesAdded());
                    fileInfo.put("deletions", file.getLinesDeleted());
                    fileInfo.put("status", file.getStatus());
                    simplifiedFiles.add(fileInfo);
                }

                dataToCache.put("commit", simplifiedCommit);
                dataToCache.put("changedFiles", simplifiedFiles);
                dataToCache.put("owner", owner);
                dataToCache.put("repo", repo);

                redisTemplate.opsForValue().set(cacheKey, dataToCache, 10, TimeUnit.MINUTES);

                return ResponseEntity.ok(dataToCache);
            }
        } catch (IOException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch commit details: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHCommit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommitController {

    private final GithubService githubService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/api/repos/{owner}/{repo}/commits")
    public ResponseEntity<Map<String, Object>> getCommits(
            @PathVariable String owner,
            @PathVariable String repo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int perPage) {
        try {
            String cacheKey = "commits:" + owner + ":" + repo + ":" + page + ":" + perPage;

            @SuppressWarnings("unchecked")
            Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                return ResponseEntity.ok(cachedData);
            } else {
                List<GHCommit> commits = githubService.getRecentCommits(owner, repo, page, perPage);

                // GHCommit 객체를 직렬화 가능한 간단한 Map 객체로 변환
                List<Map<String, Object>> simplifiedCommits = new ArrayList<>();
                for (GHCommit commit : commits) {
                    Map<String, Object> commitInfo = new HashMap<>();
                    commitInfo.put("sha", commit.getSHA1());
                    commitInfo.put("authorName", commit.getAuthor().getName());
                    commitInfo.put("authorEmail", commit.getAuthor().getEmail());
                    commitInfo.put("commitDate", commit.getCommitDate());
                    commitInfo.put("message", commit.getCommitShortInfo().getMessage());
                    simplifiedCommits.add(commitInfo);
                }

                Map<String, Object> dataToCache = new HashMap<>();
                dataToCache.put("commits", simplifiedCommits); // 변환된 객체 저장
                dataToCache.put("repoName", repo);
                dataToCache.put("owner", owner);
                dataToCache.put("currentPage", page);
                dataToCache.put("perPage", perPage);

                redisTemplate.opsForValue().set(cacheKey, dataToCache, 10, TimeUnit.MINUTES);

                return ResponseEntity.ok(dataToCache);
            }
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Failed to fetch commits: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}

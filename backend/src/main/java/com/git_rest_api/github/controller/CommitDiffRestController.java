package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
public class CommitDiffRestController {

    private final GithubService githubService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/api/repos/{owner}/{repo}/commit/{sha}/file")
    public Map<String, String> getFileDiff(
            @PathVariable String owner,
            @PathVariable String repo,
            @PathVariable String sha,
            @RequestParam String filePath) throws IOException {

        // 캐시 키 생성
        String cacheKey = "file_diff:" + owner + ":" + repo + ":" + sha + ":" + filePath;

        // 캐시에서 데이터 조회
        @SuppressWarnings("unchecked")
        Map<String, String> cachedDiff = (Map<String, String>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedDiff != null) {
            return cachedDiff;
        }

        // 캐시된 데이터가 없으면 GitHub API 호출
        String diff = githubService.getFileDiff(owner, repo, sha, filePath);
        Map<String, String> result = githubService.parseFileDiff(diff);

        // 결과를 Redis에 저장 (30분 만료)
        redisTemplate.opsForValue().set(cacheKey, result, 30, TimeUnit.MINUTES);

        return result;
    }
}

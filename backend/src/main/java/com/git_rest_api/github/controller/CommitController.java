package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHCommit;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class CommitController {

    private final GithubService githubService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/repos/{owner}/{repo}/commits")
    public String viewCommits(@PathVariable String owner,
                              @PathVariable String repo,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int perPage,
                              Model model) {
        try {
            // 캐시 키 생성 - 페이지 정보 포함
            String cacheKey = "commits:" + owner + ":" + repo + ":" + page + ":" + perPage;

            // 캐시에서 데이터 조회
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                // 캐시된 데이터가 있으면 모델에 추가
                model.addAllAttributes(cachedData);
            } else {
                // 캐시된 데이터가 없으면 GitHub API 호출
                List<GHCommit> commits = githubService.getRecentCommits(owner, repo, page, perPage);

                // 모델에 데이터 추가
                model.addAttribute("commits", commits);
                model.addAttribute("repoName", repo);
                model.addAttribute("owner", owner);
                model.addAttribute("currentPage", page);
                model.addAttribute("perPage", perPage);

                // 모델 데이터를 캐시에 저장
                Map<String, Object> dataToCache = new HashMap<>();
                dataToCache.put("commits", commits);
                dataToCache.put("repoName", repo);
                dataToCache.put("owner", owner);
                dataToCache.put("currentPage", page);
                dataToCache.put("perPage", perPage);

                redisTemplate.opsForValue().set(cacheKey, dataToCache, 30, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            model.addAttribute("error", "커밋 내역 조회 중 오류 발생: " + e.getMessage());
        }
        return "commits";
    }
}

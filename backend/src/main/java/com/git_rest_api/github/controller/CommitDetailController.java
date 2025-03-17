package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Controller
@RequiredArgsConstructor
public class CommitDetailController {

    private final GithubService githubService;
    private final RedisTemplate<String, Object> redisTemplate;

    @GetMapping("/repos/{owner}/{repo}/commit/{sha}")
    public String viewCommitDetail(@PathVariable String owner,
                                   @PathVariable String repo,
                                   @PathVariable String sha,
                                   Model model) {
        try {
            // 캐시 키 생성
            String cacheKey = "commit_detail:" + owner + ":" + repo + ":" + sha;

            // 캐시에서 데이터 조회
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedData = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedData != null) {
                // 캐시된 데이터가 있으면 모델에 추가
                model.addAllAttributes(cachedData);
            } else {
                // 캐시된 데이터가 없으면 GitHub API 호출
                GHCommit commit = githubService.getCommitDetail(owner, repo, sha);
                List<File> changedFiles = commit.getFiles();

                // 모델에 데이터 추가
                model.addAttribute("commit", commit);
                model.addAttribute("changedFiles", changedFiles);
                model.addAttribute("owner", owner);
                model.addAttribute("repo", repo);

                // 직렬화 가능한 간단한 데이터만 캐시에 저장
                Map<String, Object> dataToCache = new HashMap<>();
                // GHCommit 객체에서 필요한 정보만 추출
                Map<String, Object> simplifiedCommit = new HashMap<>();
                simplifiedCommit.put("sha", commit.getSHA1());
                simplifiedCommit.put("authorName", commit.getAuthor().getName());
                simplifiedCommit.put("authorEmail", commit.getAuthor().getEmail());
                simplifiedCommit.put("commitDate", commit.getCommitDate());
                simplifiedCommit.put("message", commit.getCommitShortInfo().getMessage());

                // 변경된 파일 정보도 간소화
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

                redisTemplate.opsForValue().set(cacheKey, dataToCache, 30, TimeUnit.MINUTES);
            }
        } catch (IOException e) {
            model.addAttribute("error", "커밋 상세 정보 조회 중 오류 발생: " + e.getMessage());
        }
        return "commit-detail";
    }
}

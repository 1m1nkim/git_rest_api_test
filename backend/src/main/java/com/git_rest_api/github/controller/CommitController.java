package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHCommit;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommitController {

    private final GithubService githubService;

    @GetMapping("/repos/{owner}/{repo}/commits")
    public String viewCommits(@PathVariable String owner,
                              @PathVariable String repo,
                              @RequestParam(defaultValue = "1") int page,
                              @RequestParam(defaultValue = "10") int perPage,
                              Model model) {
        try {
            List<GHCommit> commits = githubService.getRecentCommits(owner, repo, page, perPage);
            model.addAttribute("commits", commits);
            model.addAttribute("repoName", repo);
            model.addAttribute("owner", owner);
            model.addAttribute("currentPage", page);
            model.addAttribute("perPage", perPage);
        } catch (Exception e) {
            model.addAttribute("error", "커밋 내역 조회 중 오류 발생: " + e.getMessage());
        }
        return "commits";
    }
}

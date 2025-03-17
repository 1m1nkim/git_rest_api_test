package com.git_rest_api.github.controller;

import com.git_rest_api.github.service.GithubService;
import lombok.RequiredArgsConstructor;
import org.kohsuke.github.GHCommit;
import org.kohsuke.github.GHCommit.File;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class CommitDetailController {

    private final GithubService githubService;

    @GetMapping("/repos/{owner}/{repo}/commit/{sha}")
    public String viewCommitDetail(@PathVariable String owner,
                                   @PathVariable String repo,
                                   @PathVariable String sha,
                                   Model model) {
        try {
            // 커밋 상세 정보 가져오기
            GHCommit commit = githubService.getCommitDetail(owner, repo, sha);

            // 변경된 파일 목록
            List<File> changedFiles = commit.getFiles();

            // 커밋 정보와 변경된 파일 목록을 모델에 추가
            model.addAttribute("commit", commit);
            model.addAttribute("changedFiles", changedFiles);
            model.addAttribute("owner", owner);
            model.addAttribute("repo", repo);

        } catch (IOException e) {
            model.addAttribute("error", "커밋 상세 정보 조회 중 오류 발생: " + e.getMessage());
        }

        return "commit-detail";
    }

    @GetMapping("/repos/{owner}/{repo}/commit/{sha}/file")
    public String viewFileDiff(@PathVariable String owner,
                               @PathVariable String repo,
                               @PathVariable String sha,
                               @PathVariable String filePath,
                               Model model) {
        try {
            // 특정 파일의 diff 정보 가져오기
            String diff = githubService.getFileDiff(owner, repo, sha, filePath);

            model.addAttribute("diff", diff);
            model.addAttribute("filePath", filePath);
            model.addAttribute("owner", owner);
            model.addAttribute("repo", repo);
            model.addAttribute("sha", sha);

        } catch (IOException e) {
            model.addAttribute("error", "파일 변경 내역 조회 중 오류 발생: " + e.getMessage());
        }

        return "file-diff";
    }
}

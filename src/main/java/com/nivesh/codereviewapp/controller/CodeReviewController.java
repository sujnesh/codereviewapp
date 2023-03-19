package com.nivesh.codereviewapp.controller;

import com.nivesh.codereviewapp.model.CodeReviewRequest;
import com.nivesh.codereviewapp.model.CodeReviewResponse;
import com.nivesh.codereviewapp.service.ChatGPTService;
import com.nivesh.codereviewapp.service.RepoService;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CodeReviewController {

    @Autowired
    private RepoService repoService;

    @Autowired
    private ChatGPTService chatGPTService;

    @PostMapping("/api/code-review")
    public ResponseEntity<CodeReviewResponse> submitCodeReview(@RequestBody CodeReviewRequest request) throws JSONException {
        List<String> codeChanges = repoService.fetchCodeChanges(request);
        List<String> codeReviewComments = chatGPTService.getCodeReviewComments(codeChanges);

        CodeReviewResponse response = new CodeReviewResponse();
        response.setComments(codeReviewComments);
        return ResponseEntity.ok(response);
    }
}

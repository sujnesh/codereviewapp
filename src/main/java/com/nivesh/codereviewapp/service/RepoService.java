package com.nivesh.codereviewapp.service;

import com.nivesh.codereviewapp.model.CodeReviewRequest;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class RepoService {

    //    @Value("${github.token}")
    private String githubToken = "";

    private RestTemplate restTemplate = new RestTemplate();

    public List<String> fetchCodeChanges(CodeReviewRequest request) throws JSONException {
        String[] urlParts = request.getPrUrl().split("/");
        String owner = urlParts[3];
        String repo = urlParts[4];
        String prNumber = urlParts[6];

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.github.com/repos/{owner}/{repo}/pulls/{pull_number}",
                HttpMethod.GET,
                entity,
                String.class,
                owner, repo, prNumber
        );

        String diffUrl = new JSONObject(response.getBody()).getString("diff_url");

        ResponseEntity<String> diffResponse = restTemplate.exchange(
                diffUrl,
                HttpMethod.GET,
                entity,
                String.class
        );

        List<String> codeChanges = processDiff(diffResponse.getBody());

        return codeChanges;
    }

    private List<String> processDiff(String diff) {
        String[] lines = diff.split("\n");
        List<String> codeChanges = new ArrayList<>();

        for (String line : lines) {
            if (line.startsWith("+") && !line.startsWith("+++")) {
                codeChanges.add(line.substring(1).trim());
            }
        }

        return codeChanges;
    }
}

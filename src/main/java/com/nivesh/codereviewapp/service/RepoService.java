package com.nivesh.codereviewapp.service;

import com.nivesh.codereviewapp.model.CodeReviewRequest;
import org.json.JSONArray;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
                "https://patch-diff.githubusercontent.com/raw/{owner}/{repo}/pull/{pull_number}.patch",
                HttpMethod.GET,
                entity,
                String.class,
                owner, repo, prNumber
        );

//        String diffUrl = new JSONObject(response.getBody()).getString("diff_url");
//
//        ResponseEntity<String> diffResponse = restTemplate.exchange(
//                diffUrl,
//                HttpMethod.GET,
//                entity,
//                String.class
//        );

//        List<String> codeChanges = processDiff(diffResponse.getBody());
        List<String> codeChanges = processDiff(response.getBody());
        return codeChanges;
    }

    public CodeReviewRequest fetchPrMetadata(CodeReviewRequest request) throws JSONException {
        String[] urlParts = request.getPrUrl().split("/");
        String owner = urlParts[3];
        String repo = urlParts[4];
        String prNumber = urlParts[6];

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "token " + githubToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // Fetch PR metadata
        ResponseEntity<String> metadataResponse = restTemplate.exchange(
                "https://api.github.com/repos/{owner}/{repo}/pulls/{pull_number}",
                HttpMethod.GET,
                entity,
                String.class,
                owner, repo, prNumber
        );

        ResponseEntity<String> commitsResponse = restTemplate.exchange(
                "https://api.github.com/repos/{owner}/{repo}/pulls/{pull_number}/commits",
                HttpMethod.GET,
                entity,
                String.class,
                owner, repo, prNumber
        );

        JSONArray commitsArray = new JSONArray(commitsResponse.getBody());
        List<String> commitMessages = new ArrayList<>();

        for (int i = 0; i < commitsArray.length(); i++) {
            JSONObject commitJson = commitsArray.getJSONObject(i);
            String message = commitJson.getJSONObject("commit").getString("message");
            commitMessages.add(message);
        }

        request.setCommitMessages(commitMessages);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://patch-diff.githubusercontent.com/raw/{owner}/{repo}/pull/{pull_number}.patch",
                HttpMethod.GET,
                entity,
                String.class,
                owner, repo, prNumber
        );
        List<String> codeChanges = processDiff(response.getBody());
        List<String> newMethods = extractNewMethods(codeChanges);

        JSONObject metadataJson = new JSONObject(metadataResponse.getBody());
        request.setPrTitle(metadataJson.getString("title"));
        request.setPrDescription(metadataJson.getString("body"));
        request.setPrAuthor(metadataJson.getJSONObject("user").getString("login"));
        request.setNewMethods(newMethods);

        return request;
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

    private List<String> extractNewMethods(List<String> codeChanges) {
        List<String> newMethods = new ArrayList<>();
        Pattern methodPattern = Pattern.compile("^\\s*(public|private|protected)?\\s+((?!static)[\\w<>])+\\s+([a-zA-Z0-9_]+)\\s*\\(");
        int maxTokensPerMethod = 2000; // You can adjust this value based on your requirements.

        boolean inMethod = false;
        StringBuilder methodBody = new StringBuilder();
        int braceCount = 0;

        for (String codeChange : codeChanges) {
            if (inMethod) {
                methodBody.append(codeChange).append("\n");
                braceCount += countBraces(codeChange);

                if (braceCount == 0) {
                    inMethod = false;
                    if (methodBody.length() <= maxTokensPerMethod) {
                        newMethods.add(methodBody.toString());
                    }
                    methodBody = new StringBuilder();
                }
            } else {
                Matcher matcher = methodPattern.matcher(codeChange);
                if (matcher.find()) {
                    inMethod = true;
                    methodBody.append(codeChange).append("\n");
                    braceCount += countBraces(codeChange);
                }
            }
        }

        return newMethods;
    }

    private int countBraces(String codeLine) {
        int count = 0;
        for (char c : codeLine.toCharArray()) {
            if (c == '{') {
                count++;
            } else if (c == '}') {
                count--;
            }
        }
        return count;
    }
}

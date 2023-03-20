package com.nivesh.codereviewapp.service;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatGPTService {

    //    @Value("${openai.api_key}")
    private String openaiApiKey = "";

    private RestTemplate restTemplate = new RestTemplate();

    public List<String> getCodeReviewComments(List<String> codeChanges) throws JSONException {
        List<String> codeReviewComments = new ArrayList<>();

        List<String> codeChangeBatch = new ArrayList<>();
        int tokenCount = 0;
        int maxTokensPerBatch = 2000; // You can adjust this value based on your requirements.

        for (String codeChange : codeChanges) {
            int lineTokens = codeChange.length() + 1; // Add 1 for newline character.
            if (tokenCount + lineTokens > maxTokensPerBatch) {
                List<String> batchComments = requestCodeReview(codeChangeBatch);
                codeReviewComments.addAll(batchComments);
                codeChangeBatch.clear();
                tokenCount = 0;
            }
            codeChangeBatch.add(codeChange);
            tokenCount += lineTokens;
        }

        // Process the last batch if any code changes remain.
        if (!codeChangeBatch.isEmpty()) {
            List<String> batchComments = requestCodeReview(codeChangeBatch);
            codeReviewComments.addAll(batchComments);
        }

        return codeReviewComments;
    }

    private List<String> requestCodeReview(List<String> codeChanges) throws JSONException {
        String prompt = createPrompt(codeChanges);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text-davinci-003");
        requestBody.put("prompt", prompt);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 500);
        requestBody.put("n", 1);
        requestBody.put("stop", null);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.openai.com/v1/completions",
                HttpMethod.POST,
                new HttpEntity<>(requestBody.toString(), headers),
                String.class
        );

        JSONArray choices = new JSONObject(response.getBody()).getJSONArray("choices");
        JSONObject choice = choices.getJSONObject(0);
        String codeReview = choice.getString("text");

        return parseCodeReview(codeReview);
    }

    private String createPrompt(List<String> codeChanges) {

        StringBuilder prompt = new StringBuilder("I have Java code changes and need a comprehensive and non-generic code review with suggestions included wherever possible.Also include critical design improvement suggestions. Here are the changes:\n\n");

        for (String codeChange : codeChanges) {
            prompt.append(codeChange).append("\n");
        }

//        prompt.append("\nPlease provide a code review with comments and suggestions.");

        return prompt.toString();
    }

    private List<String> parseCodeReview(String codeReview) {
        String[] lines = codeReview.split("\n");
        List<String> codeReviewComments = new ArrayList<>();

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                codeReviewComments.add(line.trim());
            }
        }

        return codeReviewComments;
    }
}

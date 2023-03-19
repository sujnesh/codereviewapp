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
    private String openaiApiKey = "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";

    private RestTemplate restTemplate = new RestTemplate();

    public List<String> getCodeReviewComments(List<String> codeChanges) throws JSONException {
        String prompt = createPrompt(codeChanges);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + openaiApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "text-davinci-002");
        requestBody.put("prompt", prompt);
        requestBody.put("temperature", 0.5);
        requestBody.put("max_tokens", 200);

        ResponseEntity<String> response = restTemplate.exchange(
                "https://api.openai.com/v1/engines/text-davinci-002/completions",
                HttpMethod.POST,
                new HttpEntity<>(requestBody.toString(), headers),
                String.class
        );

        JSONArray choices = new JSONObject(response.getBody()).getJSONArray("choices");
        JSONObject choice = choices.getJSONObject(0);
        String codeReview = choice.getString("text");

        List<String> codeReviewComments = parseCodeReview(codeReview);

        return codeReviewComments;
    }

    private String createPrompt(List<String> codeChanges) {
        StringBuilder prompt = new StringBuilder("I have some Java code changes and need a code review. Here are the changes:\n\n");

        for (String codeChange : codeChanges) {
            prompt.append(codeChange).append("\n");
        }

        prompt.append("\nPlease provide a code review with comments and suggestions.");

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

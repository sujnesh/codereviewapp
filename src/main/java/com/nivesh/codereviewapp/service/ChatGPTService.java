package com.nivesh.codereviewapp.service;

import com.nivesh.codereviewapp.model.CodeReviewRequest;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChatGPTService {
    @Autowired
    private RepoService repoService;

    //    @Value("${openai.api_key}")
    private String openaiApiKey = "";

    private RestTemplate restTemplate = new RestTemplate();

    public List<String> getCodeReviewComments(List<String> codeChanges) throws JSONException {
        List<String> codeReviewComments = new ArrayList<>();

        List<String> codeChangeBatch = new ArrayList<>();
        int tokenCount = 0;
        int maxTokensPerBatch = 4000; // You can adjust this value based on your requirements.

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

    public String getPrSummary(CodeReviewRequest request) throws JSONException {
        request = repoService.fetchPrMetadata(request);
        String prompt = createPrSummaryPrompt(request);

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
        String prSummary = choice.getString("text");

        return prSummary.trim();
    }

    private String createPrompt(List<String> codeChanges) {

//        StringBuilder prompt = new StringBuilder("I have Java code changes and need a comprehensive and non-generic code review with suggestions included wherever possible.Also include critical design improvement suggestions. Here are the changes:\n\n");

        String finalCodeChange = "";
        for(String word: codeChanges){
            finalCodeChange += word;
            finalCodeChange += "\n";
        }

        StringBuilder prompt = new StringBuilder("Act as a code reviewer of a Pull Request, providing feedback on the code changes below.\n" +
                "  You are provided with the Pull Request changes in a patch format.\n" +
                "  Each patch entry has the commit message in the Subject line followed by the code changes (diffs) in a unidiff format.\n" +
                "  \\n\\n\n" +
                "  Patch of the Pull Request to review:\n" +
                "  \\n\n" + finalCodeChange +
                "  {patch}\n" +
                "  \\n\\n\n" +
                "  \n" +
                "  As a code reviewer, your task is:\n" +
                "  - Review the code changes (diffs) in the patch and provide feedback.\n" +
                "  - If there are any bugs, highlight them.\n" +
                "  - Does the code do what it says in the commit messages?\n" +
                "  - Do not highlight minor issues and nitpicks.\n" +
                "  - Use bullet points if you have multiple comments.`");

//        for (String codeChange : codeChanges) {
//            prompt.append(codeChange).append("\n");
//        }

//        prompt.append("\nPlease provide a code review with comments and suggestions.");

        return prompt.toString();
    }

    private String createPrSummaryPrompt(CodeReviewRequest request) {
        String prTitle = request.getPrTitle();
        String prBody = request.getPrDescription();

        StringBuilder prompt = new StringBuilder("As an AI, provide a concise and informative summary of the following pull request. We've mentioned tehe details below. Try to provide as much info as possible and also try to summarise the new methods introduced and what they are trying to achieve. Also if there is some issue in the new methods highlight them as well. At the end try to decipher what all these code changes are cumulatively trying to do. In case you need any extra info you can ask for it and we'll add it in the description of the pr.\n\n");

        prompt.append("Pull Request Title: ").append(prTitle).append("\n");
        prompt.append("Pull Request Description: ").append(prBody).append("\n");

        prompt.append("\nCommit Messages:\n");
        for (String commitMessage : request.getCommitMessages()) {
            prompt.append("- ").append(commitMessage).append("\n");
        }

        prompt.append("\nNew Methods Introduced:\n");
        for (String newMethod : request.getNewMethods()) {
            prompt.append("\n```\n");
            prompt.append(newMethod);
            prompt.append("```\n");
            prompt.append("Summary for this method: \n"); // Requesting a summary for the new method
        }

        prompt.append("\nEnd of Pull Request Information.\n");
        prompt.append("-----\n");
        prompt.append("Summary:");

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

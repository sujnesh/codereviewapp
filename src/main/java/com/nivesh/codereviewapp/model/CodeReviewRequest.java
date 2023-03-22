package com.nivesh.codereviewapp.model;

import java.util.List;

public class CodeReviewRequest {
    private String prUrl;
    private String prDescription;
    private List<String> commitMessages;
    private List<String> comments;
    private List<String> newMethods;

    public List<String> getNewMethods() {
        return newMethods;
    }

    public void setNewMethods(List<String> newMethods) {
        this.newMethods = newMethods;
    }

    String prTitle;
    String prAuthor;
    public String getPrTitle() {
        return prTitle;
    }

    public void setPrTitle(String prTitle) {
        this.prTitle = prTitle;
    }

    public String getPrAuthor() {
        return prAuthor;
    }

    public void setPrAuthor(String prAuthor) {
        this.prAuthor = prAuthor;
    }

    public String getPrDescription() {
        return prDescription;
    }

    public void setPrDescription(String prDescription) {
        this.prDescription = prDescription;
    }

    public List<String> getCommitMessages() {
        return commitMessages;
    }

    public void setCommitMessages(List<String> commitMessages) {
        this.commitMessages = commitMessages;
    }

    public List<String> getComments() {
        return comments;
    }

    public void setComments(List<String> comments) {
        this.comments = comments;
    }

    public String getPrUrl() {
        return prUrl;
    }

    public void setPrUrl(String prUrl) {
        this.prUrl = prUrl;
    }
}

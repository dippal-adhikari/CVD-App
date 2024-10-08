package com.example.cvd_draft_1;

import java.util.List;

public class Script {
    private String id;
    private String createdAt;
    private List<String> questions;
    private List<String> answers;

    // Default constructor
    public Script() {}

    // Constructor with fields
    public Script(String id, String createdAt, List<String> questions, List<String> answers) {
        this.id = id;
        this.createdAt = createdAt;
        this.questions = questions;
        this.answers = answers;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getQuestions() {
        return questions;
    }

    public void setQuestions(List<String> questions) {
        this.questions = questions;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }
}

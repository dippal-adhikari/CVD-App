package com.example.cvd_draft_1.api;

import java.util.List;

public class OpenAIRequest {
    private String model;
    private List<Message> messages;
    private int max_tokens;

    public OpenAIRequest(String model, List<Message> messages, int maxTokens) {
        this.model = model;
        this.messages = messages;
        this.max_tokens = maxTokens;
    }

    public static class Message {
        private String role;
        private String content;

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }
}

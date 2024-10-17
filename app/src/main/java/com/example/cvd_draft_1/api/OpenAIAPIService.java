package com.example.cvd_draft_1.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIAPIService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })
    @POST("v1/chat/completions")  // Updated to the correct endpoint for chat models
    Call<OpenAIResponse> createCompletion(@Body OpenAIRequest body);
}
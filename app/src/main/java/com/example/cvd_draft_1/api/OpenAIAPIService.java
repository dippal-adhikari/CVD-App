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
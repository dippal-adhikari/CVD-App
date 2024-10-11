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
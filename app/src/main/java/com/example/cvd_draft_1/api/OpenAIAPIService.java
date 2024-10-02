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
package com.example.cvd_draft_1.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIAPIService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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" })
    @POST("v1/chat/completions")  // Updated to the correct endpoint for chat models
    Call<OpenAIResponse> createCompletion(@Body OpenAIRequest body);
}
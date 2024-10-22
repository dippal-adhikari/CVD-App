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
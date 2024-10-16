package com.example.cvd_draft_1.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIAPIService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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 "
    })
    @POST("v1/chat/completions")  // Updated to the correct endpoint for chat models
    Call<OpenAIResponse> createCompletion(@Body OpenAIRequest body);
}
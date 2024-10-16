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
package com.example.cvd_draft_1.api;

import com.example.cvd_draft_1.api.OpenAIRequest;
import com.example.cvd_draft_1.api.OpenAIResponse;

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

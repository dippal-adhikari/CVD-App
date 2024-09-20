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

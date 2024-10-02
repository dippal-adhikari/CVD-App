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
            "Authorization: Bearer"
//            sk-gq-DIT3IKmuR64-K9JeYQfyUoK4syK_C9LXc8ooMxmT3BlbkFJsOQc1a97vuBiji8fVHRN-wTfkyDRXSo6t2_D7AqSsA
    })
    @POST("v1/chat/completions")  // Updated to the correct endpoint for chat models
    Call<OpenAIResponse> createCompletion(@Body OpenAIRequest body);
}

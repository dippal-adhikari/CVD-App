package com.example.cvd_draft_1.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface OpenAIAPIService {

    @POST("v1/chat/completions")  // Updated to the correct endpoint for chat models
    Call<OpenAIResponse> createCompletion(@Body OpenAIRequest body);
}

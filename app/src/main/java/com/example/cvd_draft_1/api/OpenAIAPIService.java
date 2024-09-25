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
// <<<<<<< main
//             "Authorization: Bearer "
// =======
//             "Authorization: Bearer"
// >>>>>>> main
    })
    @POST("v1/chat/completions")  // Updated to the correct endpoint for chat models
    Call<OpenAIResponse> createCompletion(@Body OpenAIRequest body);
}

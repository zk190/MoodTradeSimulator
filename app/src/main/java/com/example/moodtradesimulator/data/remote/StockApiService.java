package com.example.moodtradesimulator.data.remote;

import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface StockApiService {

    @GET("quote")
    Call<JsonObject> getQuotes(
            @Query("symbol") String symbols,
            @Query("apikey") String apiKey
    );
}

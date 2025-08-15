package com.example.aroundme.data.remote

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApi {
    @GET("interpreter")
    suspend fun getTouristAttractions(
        @Query("data") query: String
    ): Response<JsonObject>
}

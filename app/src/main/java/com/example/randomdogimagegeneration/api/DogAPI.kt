package com.example.randomdogimagegeneration.api

import com.example.randomdogimagegeneration.model.Dog
import retrofit2.Response
import retrofit2.http.GET

interface DogAPI {
    @GET("random")
    suspend fun getRandomDog(): Response<Dog>
}
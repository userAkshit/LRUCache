package  com.example.randomdogimagegeneration.api

import com.example.randomdogimagegeneration.utils.Constant
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    val api: DogAPI by lazy {
        Retrofit.Builder().baseUrl(Constant.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DogAPI::class.java)
    }
}
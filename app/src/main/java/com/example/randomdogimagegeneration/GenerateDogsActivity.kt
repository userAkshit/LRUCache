package com.example.randomdogimagegeneration

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.example.randomdogimagegeneration.api.RetrofitInstance
import com.example.randomdogimagegeneration.databinding.ActivityGenerateDogsBinding
import com.example.randomdogimagegeneration.glide.GlideApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.net.URL


private const val TAG = "GenerateDogsActivity"

class GenerateDogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGenerateDogsBinding
    private lateinit var imageCache: ImageCache
    private var isGeneratingImage = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGenerateDogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageCache = ImageCache.getInstance(this)

        binding.btnGenerateImage.setOnClickListener {
            isGeneratingImage = true
            changeButtonColor()

            lifecycleScope.launch {
                binding.progressBar.isVisible = true
                val response = try {
                    withContext(Dispatchers.IO) {
                        RetrofitInstance.api.getRandomDog()
                    }
                } catch (e: IOException) {
                    binding.progressBar.isVisible = false
                    isGeneratingImage = false
                    changeButtonColor()
                    //Log.e(TAG, "IOException, You might not have internet connection")
                    //Toast.makeText(this@GenerateDogsActivity, "Try again", Toast.LENGTH_SHORT).show()
                    return@launch
                } catch (e: HttpException) {
                    binding.progressBar.isVisible = false
                    isGeneratingImage = false
                    changeButtonColor()
                    //Toast.makeText(this@GenerateDogsActivity, "Try again", Toast.LENGTH_SHORT).show()
                    //Log.e(TAG, "HttpException, unexpected response")
                    return@launch
                }

                if (response.isSuccessful && response.body() != null) {
                    //Log.e(TAG, "message: " + response.body()!!.message)
                    //Log.e(TAG, "status: " + response.body()!!.status)

                    if (response.body() != null && response.body()!!.status.equals("success", true)) {
                        binding.progressBar.isVisible = false
                        isGeneratingImage = false
                        changeButtonColor()

                        try {
                            val url = URL(response.body()!!.message)
                            var image: Bitmap? = null
                            withContext(Dispatchers.IO) {
                                image = BitmapFactory.decodeStream(withContext(Dispatchers.IO) {
                                    url.openConnection().getInputStream()
                                })

                                if (image != null) {
                                    imageCache.put(response.body()!!.message, image!!)

                                    withContext(Dispatchers.Main) {
                                        GlideApp.with(this@GenerateDogsActivity)
                                            .load(response.body()!!.message)
                                            .into(binding.imgDogImage)
                                    }
                                }
                            }
                        } catch (e: IOException) {
                            isGeneratingImage = false
                            changeButtonColor()
                        }
                    }
                } else {
                    binding.progressBar.isVisible = false
                    //Toast.makeText(this@GenerateDogsActivity, "Response Failed", Toast.LENGTH_SHORT);
                    //Log.e(TAG, "Response not successful")
                }
            }
        }
    }

    private fun changeButtonColor() {
        binding.btnGenerateImage.isEnabled = !isGeneratingImage
        binding.btnGenerateImage.isClickable = !isGeneratingImage
        binding.btnGenerateImage.setBackgroundColor(if (isGeneratingImage) resources.getColor(R.color.btn_disable_color, null) else resources.getColor(R.color.btn_color, null))
    }
}
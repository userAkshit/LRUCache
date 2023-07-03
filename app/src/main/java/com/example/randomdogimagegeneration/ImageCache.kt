package com.example.randomdogimagegeneration

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.collection.LruCache
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.IOException
import java.net.URL

private const val TAG = "ImageCache"

class ImageCache private constructor(private val context: Context) {

    init {
        val cacheSize = 20

        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        //val cacheSize = maxMemory / 8

        cache = object : LruCache<String, Bitmap>(cacheSize) {
            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                return bitmap.byteCount / 1024
            }
        }
    }

    companion object {
        private lateinit var cache: LruCache<String, Bitmap>

        private const val CACHE_PREFS_NAME = "ImageCachePrefs"
        private const val CACHE_KEY = "cachedImageUrls"

        private var instance: ImageCache? = null

        fun getInstance(context: Context): ImageCache {
            return instance ?: synchronized(this) {
                instance ?: ImageCache(context).also { instance = it }
            }
        }
    }

    fun put(key: String, bitmap: Bitmap) {
        if (get(key) == null) {
            if (cache.size() >= cache.maxSize()) {
                val firstKey = cache.snapshot().keys.firstOrNull()
                firstKey?.let {
                    cache.remove(it)
                }
            }
            cache.put(key, bitmap)
            saveCachedImageUrls(key)
        }
    }

    fun get(key: String): Bitmap? {
        return cache.get(key)
    }

    fun remove(key: String) {
        cache.remove(key)
        saveCachedImageUrls()
    }

    fun clear() {
        cache.evictAll()
        saveCachedImageUrls("")
    }

    fun snapshot(): LinkedHashMap<String, Bitmap> {
        return LinkedHashMap(cache.snapshot())
    }

    fun loadCachedImageUrls(): List<String> {
        val prefs = context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)
        val urlsJson = prefs.getString(CACHE_KEY, null)
        Log.e(TAG, "loadCachedImageUrls - urlsJson: $urlsJson")
        return urlsJson?.let {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson(it, type)
        } ?: emptyList()
    }

    private fun saveCachedImageUrls(newUrl: String? = null) {
        val urls = loadCachedImageUrls().toMutableSet()
        if (newUrl?.isEmpty() == true) {
            urls.clear()
        } else if (newUrl != null) {
            urls.add(newUrl)
            if (urls.size > 20) {
                val element = urls.first()
                urls.remove(element)
            }
        }
        val urlsJson = Gson().toJson(urls)
        Log.e(TAG, "saveCachedImageUrls - urlsJson: $urlsJson")

        val prefs = context.getSharedPreferences(CACHE_PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(CACHE_KEY, urlsJson).apply()
    }
}

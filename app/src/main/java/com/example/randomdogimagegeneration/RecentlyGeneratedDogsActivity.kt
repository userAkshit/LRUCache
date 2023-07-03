package com.example.randomdogimagegeneration

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.randomdogimagegeneration.databinding.ActivityRecentlyGeneratedDogsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val TAG = "RecentlyGeneratedDogsAc"

class RecentlyGeneratedDogsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRecentlyGeneratedDogsBinding
    private lateinit var imageCache: ImageCache
    private lateinit var adapter: DogImagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecentlyGeneratedDogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        imageCache = ImageCache.getInstance(this)
        lifecycleScope.launch {
            binding.rvImages.layoutManager = LinearLayoutManager(this@RecentlyGeneratedDogsActivity, LinearLayoutManager.HORIZONTAL, false)
            withContext(Dispatchers.IO) {
                val imageUrls = imageCache.loadCachedImageUrls()
                adapter = DogImagesAdapter(imageCache, imageUrls.reversed())
                withContext(Dispatchers.Main) {
                    binding.rvImages.adapter = adapter
                }
            }
        }

        binding.btnClearData.setOnClickListener {
            clearDogs()
        }
    }

    private fun clearDogs() {
        imageCache.clear()
        adapter.updateData(emptyList())
    }

    class DogImagesAdapter(private val imageCache: ImageCache, private var dogImageUrls: List<String>) : RecyclerView.Adapter<DogImagesAdapter.DogImageViewHolder>() {

        class DogImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val imageView: ImageView = itemView.findViewById(R.id.imageView)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DogImageViewHolder {
            val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_dog_image, parent, false)
            return DogImageViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: DogImageViewHolder, position: Int) {
            val imageUrl = dogImageUrls[position]

            // Load image from cache if available
            val imageBitmap = imageCache.get(imageUrl)

            if (imageBitmap != null) {
                holder.imageView.setImageBitmap(imageBitmap)
            } else {
                // Load image using Glide
                Glide.with(holder.itemView.context)
                    .load(imageUrl)
                    .into(holder.imageView)
            }
        }

        override fun getItemCount(): Int {
            return dogImageUrls.size
        }

        fun updateData(dogImageUrls: List<String>) {
            this.dogImageUrls = dogImageUrls
            notifyDataSetChanged()
        }
    }
}